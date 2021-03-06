package org.march.jackson;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

@JsonTypeInfo(  
	    use = JsonTypeInfo.Id.NAME,  
	    include = JsonTypeInfo.As.PROPERTY,  
	    property = "@type")  
//@JsonSubTypes({
//	@Type(value = FullCopy.class, name = "Update"),
//    @Type(value = UpdateChangeSet.class, name = "Synchronize")})
public abstract class MessageMixin {

}
