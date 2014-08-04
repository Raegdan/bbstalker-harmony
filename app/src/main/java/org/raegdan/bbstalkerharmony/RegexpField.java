package org.raegdan.bbstalkerharmony;

public class RegexpField implements Cloneable {
	public String regexp;
	public String field;
	public Integer priority;

	public RegexpField clone() throws CloneNotSupportedException {
    	return (RegexpField) super.clone();
	}
}
