/*
 *
 */
package sk.antons.sbsampler.rest.mongo;

import lombok.Data;

/**
 *
 * @author antons
 */
@Data
public class Transfer {

	Type transferType;
    String transferId;
    String path;
    int count;
    String user;

    public static enum Type {
		NEW,
		CHANGED,
		CLOSED,
		RECLOSED,
		ARCHIVED
		;
    }
}
