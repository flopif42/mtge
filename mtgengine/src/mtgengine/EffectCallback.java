package mtgengine;

public class EffectCallback {
	private String _methodName;
	private Object _parameter;
	
	public EffectCallback(String name, Object parameter) {
		_methodName = name;
		_parameter = parameter;
	}
	
	public String getMethodName() {
		return _methodName;
	}
	
	public Object getParameter() {
		return _parameter;
	}
}
