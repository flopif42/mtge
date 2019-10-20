package mtgengine.action;

public interface PerformableAction {
	public enum Type { ACTIVATE_ABILITY, CAST_SPELL, TAKE_SPECIAL_ACTION };
	
	public String getSystemName();
	public int getID();
	public Type getActionType();
}
