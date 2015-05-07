package modularization;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class ModuleChainTest {

	@Test
	public void test() throws IncompatibleIOException {
		Module m1 = new Module(){

			@Override
			public void setInput(Object input) throws IncompatibleIOException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public Object getInput() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean process() throws Exception {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void setOutput(Object output) throws IncompatibleIOException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public Object getOutput() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public List<Class<?>> getInputformats() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public List<Class<?>> getOutputformats() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean doesSupportInput(Object input) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean doesSupportOutput(Object output) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public String getName() {
				return "m1";
			}
			
		};
		Module m2 = new Module(){

			@Override
			public void setInput(Object input) throws IncompatibleIOException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public Object getInput() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean process() throws Exception {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void setOutput(Object output) throws IncompatibleIOException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public Object getOutput() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public List<Class<?>> getInputformats() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public List<Class<?>> getOutputformats() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean doesSupportInput(Object input) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean doesSupportOutput(Object output) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public String getName() {
				return "m2";
			}
			
		};
		Module m3 = new Module(){

			@Override
			public void setInput(Object input) throws IncompatibleIOException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public Object getInput() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean process() throws Exception {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void setOutput(Object output) throws IncompatibleIOException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public Object getOutput() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public List<Class<?>> getInputformats() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public List<Class<?>> getOutputformats() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean doesSupportInput(Object input) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean doesSupportOutput(Object output) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public String getName() {
				return "m3";
			}
			
		};
		
		ModuleChain mc = new ModuleChain();
		mc.appendModule(m1, 0);
		mc.appendModule(m2, 1);
		mc.appendModule(m3, 1);
		
		assertTrue(true);
	}

}
