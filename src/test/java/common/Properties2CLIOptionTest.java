package common;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.Assert;
import org.junit.Test;

public class Properties2CLIOptionTest {

	@Test
	public void testGetCLIOptions() throws Exception {
		Properties properties = new Properties();
		Map<String, String> propertyDescriptions = new HashMap<String, String>();
		properties.setProperty("blabla", "blubb");
		properties.setProperty("nix", "wupp");
		properties.setProperty("zapp", "qwurks");
		properties.setProperty("zupp", "qworks");
		propertyDescriptions.put("blabla", "this is a description");
		propertyDescriptions.put("nix", "this is a description");
		propertyDescriptions.put("zapp", "this is a description");
		propertyDescriptions.put("zupp", "this is a description");
		Options options = Properties2CLIOption.getCLIOptions(properties, propertyDescriptions);
		Option blablaopt = options.getOption("blabla");
		Option nixopt = options.getOption("nix");
		Option zappopt = options.getOption("zapp");
		Option zuppopt = options.getOption("zupp");
		Assert.assertTrue (blablaopt.getOpt().equals("b"));
		Assert.assertTrue (nixopt.getOpt().equals("n"));
		// Since the underlying structure is a hashmap, we cannot say whether zupp or zapp will be processed first (and which one gets the 'z' as short option character).
		Assert.assertTrue ((zappopt.getOpt().equals("z") && zuppopt.getOpt().equals("u"))||(zappopt.getOpt().equals("a") && zuppopt.getOpt().equals("z")));
		Assert.assertTrue (zuppopt.getLongOpt().equals("zupp"));
		Assert.assertTrue (zuppopt.getDescription().equals("this is a description"));
	}

}
