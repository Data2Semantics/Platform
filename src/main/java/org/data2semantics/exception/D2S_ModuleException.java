package org.data2semantics.exception;

/**
 * 
 * Custom exception to add more information and feedback to user about 
 * exceptions happening while module is being instantiated.
 * 
 * @author wibisono
 *
 */
public class D2S_ModuleException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public D2S_ModuleException() {
		super("Module class is not defined");
	}

	public D2S_ModuleException(String message) {
		super(message);
	}
	
	public D2S_ModuleException(Throwable t){
		super(t);
	}
	
	public D2S_ModuleException(String message, Throwable t){
		super(message,t);
	}
	
}

