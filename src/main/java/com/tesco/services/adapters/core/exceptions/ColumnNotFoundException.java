package com.tesco.services.adapters.core.exceptions;
//to handle exception if any column is not available in csv extract
public class ColumnNotFoundException extends Exception {
    /**
     * <p>
     *  exception to be thrown if a particular column is not available in csv file while import process
     * </p>
     * @param message
     */
    public ColumnNotFoundException(String message) {

        super(message);
    }
}
