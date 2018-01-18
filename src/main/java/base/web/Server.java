package base.web;

import static spark.Spark.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.stream.Collectors;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;

import base.workbench.ModuleWorkbenchController;
import modules.Module;

public class Server {

	private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation()
			.create();

	private static class Message {
		@Expose
		String message;

		public Message(String message) {
			this.message = message;
		}
	}

	public static void main(String[] args) {

		// initialise the log4j properties for the web server manually
		InputStream log4jProps = Server.class.getClassLoader().getResourceAsStream("web/log4j.properties");
		if (log4jProps == null) {
			System.out.println("Refusing startup: Could not find logging properties.");
			return;
		} else {
			PropertyConfigurator.configure(log4jProps);
		}

		// Disable logging from the root logger used in the rest of the workbench
		LogManager.getLogManager().reset();

		// start the job scheduler alongside this server
		Thread jobSchedulerThread = new Thread(JobScheduler.instance());
		jobSchedulerThread.start();

		port(4568);

		post("jobs", (request, response) -> {

			Job job = (Job) GSON.fromJson(request.body(), Job.class);

			if (JobDao.exists(job.getId())) {
				throw new WebError.InvalidJobDefinition("A job for this id already exists: " + job.getId());
			}

			// test that the workflow definition is parseable
			ModuleWorkbenchController controller = new ModuleWorkbenchController();
			try {
				controller.loadModuleNetworkFromString(job.getWorkflowDefinition(), false);
			} catch (Exception e) {
				throw new WebError.InvalidWorkflowDefiniton(e);
			}

			job.save();
			JobScheduler.instance().wakeup();
			return job;

		}, GSON::toJson);

		get("jobs/:id", (request, response) -> {
			return null;
		}, GSON::toJson);

		get("health", (request, response) -> {
			return HealthInformation.collect();
		}, GSON::toJson);

		get("modules"/* , "application/json" */, (request, response) -> {

			ModuleWorkbenchController controller = new ModuleWorkbenchController();
			Map<String, Module> modules = controller.getAvailableModules();

			List<ModuleProfile> profiles = modules.values().stream().map((Module m) -> new ModuleProfile(m))
					.collect(Collectors.toList());

			response.type("application/json");
			if (profiles == null || profiles.isEmpty()) {
				LOGGER.error("No modules could be loaded.");
				return null;
			} else {
				return profiles;
			}
		}, GSON::toJson);

		exception(WebError.InvalidJobDefinition.class, (e, request, response) -> {
			response.status(400);
			Message msg = new Message("Invalid job definition: " + e.getMessage());
			response.body(GSON.toJson(msg));
		});

		exception(JsonSyntaxException.class, (e, request, response) -> {
			response.status(400);
			Message msg = new Message("Invalid input: " + e.getMessage());
			response.body(GSON.toJson(msg));
		});

		exception(WebError.InvalidWorkflowDefiniton.class, (e, request, response) -> {
			response.status(400);
			Message msg = new Message("Invalid workflow definition: " + e.getMessage());
			response.body(GSON.toJson(msg));
		});
	}
}
