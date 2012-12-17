/**
 * 
 */
package com.wcb.cms.modelmaker.brain.formatter.nosql;

/**
 * @author Zaza
 * This class represents ONE CMS Entity record.
 * NOTE here that an entity record has one or more of this kind of records...
 * 
 */
public final class CMSEntityRecord implements Comparable<CMSEntityRecord>{

	private String attribute;
	private String domain;
	private String name;

	public CMSEntityRecord(){
		domain("");
		attribute("");
		name("");
	}
	
	public CMSEntityRecord attribute(String attribute) {
		this.attribute = attribute;
		return this;
	}

	@Override
	public int compareTo(CMSEntityRecord arg0) {
		if(getName().equals(arg0.getName())){
			if(getAttribute().equals(arg0.getAttribute())){
				return getDomain().compareTo(arg0.getDomain());
			}
			//else
			return getAttribute().compareTo(arg0.getAttribute());
		}
		//else
		return getName().compareTo(arg0.getName());
		
	}

	public CMSEntityRecord domain(String domain) {
		this.domain = domain;
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		int hash = getName().hashCode()+getAttribute().hashCode()+getDomain().hashCode();
		if(obj instanceof CMSEntityRecord){
			CMSEntityRecord o = (CMSEntityRecord)obj;
			return (o.getName().hashCode()+o.getAttribute().hashCode()+o.getDomain().hashCode()) == hash;
		}
		
		return false;
	}

	public String getAttribute() {
		return attribute;
	}

	public String getDomain() {
		return domain;
	}

	public String getName() {
		return name;
	}

	public CMSEntityRecord name(String name) {
		this.name = name;
		return this;
	}
	
	@Override
	public String toString() {
		return "Name:"+getName()+"\t"+"Attribute:"+getAttribute()+"\t"+"Domain:"+getDomain();
	}
}
