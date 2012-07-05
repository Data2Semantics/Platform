package org.data2semantics.exception;


/**
 * Adding information with regards to required parameter for modules.
 * @author wibisono
 *
 */
public class D2S_ModuleParameterException extends D2S_ModuleException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public D2S_ModuleParameterException() {
		super("One of this D2S Modules parameter is not valid");
	}
	public D2S_ModuleParameterException(String message){
		super(message);
	}
	
	public D2S_ModuleParameterException(Throwable t){
		super(t);
	}
	
	public D2S_ModuleParameterException(String message, Throwable t){
		super(message,t);
	}
	

}
