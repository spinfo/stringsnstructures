package modules.seqNewickExporter;

import java.lang.reflect.Type;

import com.google.gson.InstanceCreator;


public class TreeNodeInstanceCreator implements InstanceCreator <SeqNewickNodeV2> {
	
	@Override
	public SeqNewickNodeV2 createInstance(Type type) {
		return new SeqNewickNodeV2("^");
	}
	
}