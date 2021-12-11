//Author: Steven Paredes
//abc123: obr635
package main;

import hashdb.HashFile;
import hashdb.HashHeader;
import hashdb.Vehicle;
import misc.ReturnCodes;
import misc.MutableInteger;
import misc.ParseException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.Arrays;

public class StudentFunctions extends P2Main{
    /**
     * hashCreate
     * This funcAon creates a hash file containing only the HashHeader record.
     * â€¢ If the file already exists, return RC_FILE_EXISTS
     * â€¢ Create the binary file by opening it.
     * â€¢ Write the HashHeader record to the file at RBN 0.
     * â€¢ close the file.
     * â€¢ return RC_OK.
     * 
     * This function is completed
     */
    public static int hashCreate(String fileName, HashHeader hashHeader) {
    	File f = new File(fileName);
    	if(f.exists())
			return ReturnCodes.RC_FILE_EXISTS;
    	try {
    			RandomAccessFile file = new RandomAccessFile(fileName, "rw");
    			file.write(hashHeader.toByteArray());
    			file.close();
    			
    		}catch(Exception e) {System.out.println(e); return ReturnCodes.RC_FILE_NOT_FOUND;}
    	return ReturnCodes.RC_OK;
    }

    /**
     * hashOpen
     * This function opens an existing hash file which must contain a HashHeader record
     * , and sets the file member of hashFile
     * It returns the HashHeader record by setting the HashHeader member in hashFile
     * If it doesn't exist, return RC_FILE_NOT_FOUND.
     * Read the HashHeader record from file and return it through the parameter.
     * If the read fails, return RC_HEADER_NOT_FOUND.
     * return RC_OK
     * 
     * This function is completed
     */
    public static int hashOpen(String fileName, HashFile hashFile) {
    	File f = new File(fileName);
    	try {
    		if(f.exists()) {
    			RandomAccessFile file = new RandomAccessFile(fileName, "rw");
    			
    			byte [] bytes = new byte[Vehicle.sizeOf() * 2];
    			file.read(bytes, 0, Vehicle.sizeOf() * 2);
    	        hashFile.getHashHeader().fromByteArray(bytes);
    			hashFile.setFile(file);
    		}
    		else 
    			return ReturnCodes.RC_HEADER_NOT_FOUND;
    	}catch(Exception e) {System.out.println(e); return ReturnCodes.RC_FILE_NOT_FOUND;}
    	return ReturnCodes.RC_OK;
    }
    /**
     * vehicleInsert
     * This function inserts a vehicle into the specified file.
     * Determine the RBN using the Main class' hash function.
     * Use readRec to read the record at that RBN.
     * If that location doesn't exist
     * OR the record at that location has a blank vehicleId (i.e., empty string):
     * THEN Write this new vehicle record at that location using writeRec.
     * If that record exists and that vehicle's szVehicleId matches, return RC_REC_EXISTS.
     * (Do not update it.)
     * Otherwise, it is a synonym to the vehicle in the hashed location:
	 * Determine if it exists by probing. We use a probing K value of 1. If it does already exist, 
return RC_REC_EXISTS. (Do not update it.)
• Limit the probing to hashFile.getHashHeader().getMaxProbe(). For example, if
…getMaxProbe() returns 3, you can look at the original hash location and at most two 
additional records. We are only looking at adjacent records below it.
• if there isn't an empty slot and we have probed a total of …getMaxProbe() times
(including looking at the hashed location), return RC_TOO_MANY_COLLISIONS. If it 
doesn't exist and there is an empty slot (maybe because we haven't yet written to 
that slot), write it to that empty slot.
     * !!! NEEDS TESTING !!!
     * Testing results
     * 	When inserting vehicle that already exists this error appears
     * 		"hash function received an invalid iMaxHash value: 0"
     * 	
     */
    public static int vehicleInsert(HashFile hashFile, Vehicle vehicle) {
    	 int rc;
    	 int flag = -1;
         Vehicle curr = new Vehicle();

         int RBN = hash(vehicle.getVehicleId(), hashFile.getHashHeader().getMaxHash());
         int rbn2 = -1;
         rc = readRec(hashFile, RBN, curr);
         if (curr.getVehicleIdAsString().length() == 0) {
             writeRec(hashFile, RBN, vehicle);
             return ReturnCodes.RC_OK;
         }
         else if (vehicle.getVehicleIdAsString().equals(curr.getVehicleIdAsString())) {
             return ReturnCodes.RC_REC_EXISTS;
         }
         else {
        	 int maxProbe = hashFile.getHashHeader().getMaxProbe();
        	 for(int x = RBN + 1; x < (maxProbe + RBN); x++)
        	 {
        		 curr = new Vehicle();
	           	 rc = readRec(hashFile, x, curr);
	           	 if(vehicle.getVehicleIdAsString().equals(curr.getVehicleIdAsString()))
           			 return ReturnCodes.RC_REC_EXISTS;
        	 }
    	 	 for(int x = RBN + 1; x < (maxProbe + RBN); x++)
    	 	 {
    	 		 curr = new Vehicle();
	           	 rc = readRec(hashFile, x, curr);
	           	 if(curr.getVehicleIdAsString().length() == 0 ) 
	           	 {
	           		 writeRec(hashFile, x, vehicle);
	           		 return ReturnCodes.RC_OK;
	           	 }
	           		
    	 	 }
    	 	 return ReturnCodes.RC_TOO_MANY_COLLISIONS;
	    }
    }

    /**
     * readRec(
     * This function reads a record at the specified RBN in the specified file.
     * Determine the RBA based on RBN and the HashHeader's recSize
     * Use seek to position the file in that location.
     * Read that record and return it through the vehicle parameter.
     * If the location is not found, return RC_LOC_NOT_FOUND.  Otherwise, return RC_OK.
     * Note: if the location is found, that does NOT imply that a vehicle
     * was written to that location.  Why?
     * 
	 * This function is completed
      */
    public static int readRec(HashFile hashFile, int rbn, Vehicle vehicle) {
    	/*
		int rba = rbn * hashFile.getHashHeader().getRecSize();
		int maxProbe = hashFile.getHashHeader().getMaxProbe();
		int x = rbn+1;
		
    		try {
    			hashFile.getFile().seek(rba);
    	        byte [] bytes = new byte[vehicle.sizeOf() * 2];
    	        byte [] test = new byte[vehicle.sizeOf() * 2];
    	        hashFile.getFile().read(bytes, 0, vehicle.sizeOf() * 2);
    	        if(bytes[1] != 0)
    	        	vehicle.fromByteArray(bytes);
    	        else 
    	        {
    	        	return ReturnCodes.RC_LOC_NOT_FOUND;
    	        }
    	        	
    			} 
    			catch(Exception e) {
    				return ReturnCodes.RC_LOC_NOT_FOUND;
    		}
    		return ReturnCodes.RC_OK;
    		*/
    	int rba = rbn * hashFile.getHashHeader().getRecSize();
        try {
            hashFile.getFile().seek(rba);
            byte [] bytes = new byte[Vehicle.sizeOf() * 2];
            hashFile.getFile().read(bytes, 0, Vehicle.sizeOf() * 2);
            if(bytes[1] != 0)
            	vehicle.fromByteArray(bytes);
        } catch (IOException | java.nio.BufferUnderflowException e) {
            return ReturnCodes.RC_LOC_NOT_FOUND;
        }
        return ReturnCodes.RC_OK;

    }
    /**
     * writeRec
     * This function writes a record to the specified RBN in the specified file.
     * Determine the RBA based on RBN and the HashHeader's recSize
     * Use seek to position the file in that location.
     * Write that record to the file.
     * If the write fails, return RC_LOC_NOT_WRITTEN.
     * Otherwise, return RC_OK.
     * 
     * This function is completed
     */
    public static int writeRec(HashFile hashFile, int rbn, Vehicle vehicle) {
    	/*
    	int rba = rbn * hashFile.getHashHeader().getRecSize();
    	try {
    		if(readRec(hashFile, rbn, vehicle) == ReturnCodes.RC_OK)
    			hashFile.getFile().seek(rba);
    		
            char [] chars = vehicle.toFileChars();
            
            for(int i = 0; i < chars.length; i++)
                hashFile.getFile().writeChar(chars[i]);
            
        	} 
    	catch (IOException e) {
            e.printStackTrace();
            return ReturnCodes.RC_LOC_NOT_FOUND;
        }
        return ReturnCodes.RC_OK;
        */
        int rba = rbn * hashFile.getHashHeader().getRecSize();
        try {
            hashFile.getFile().seek(rba);
            char [] chars = vehicle.toFileChars();
            for(int i = 0; i < chars.length; i++)
                hashFile.getFile().writeChar(chars[i]);
        } catch (IOException e) {
            e.printStackTrace();
            return ReturnCodes.RC_LOC_NOT_FOUND;
        }
        return ReturnCodes.RC_OK;


    }

    /**
     * vehicleRead
     * This function reads the specified vehicle by its vehicleId.
     * Since the vehicleId was provided,
     * determine the RBN using the Main class' hash function.
     * Use readRec to read the record at that RBN.
     * If the vehicle at that location matches the specified vehicleId,
     * return the vehicle via the parameter and return RC_OK.
     * PT2
     * Change your function to use rbn as a MutableInteger
     * Otherwise, it is a synonym to the vehicle in the hashed location:
     * 	Determine if it exists as a synonym using probing with a K value of 1.
     * 	Be sure to store any changed rbn in the rbn parameter!! P2Main usesit.
     * 	If vehicleIds match, return the vehicle via the vehicle parameter and return RC_OK.
     * 	If you read past the maximum records in the file, return RC_REC_NOT_FOUND.
     * 	If you have read for the maximum probes and it wasn’t found, return RC_REC_NOT_FOUND.
     *  
     *  !!! NEEDS TESTING !!!
     *  Testing results:
     *  	Gives "ERROR: record not found" when it shouldn't
     * 		Gives "ERROR: record not found" when it should
     * 			maybe the if statement is incorrect?
     *  	
     *  	
     */
    public static int vehicleRead(HashFile hashFile, MutableInteger rbn, Vehicle vehicle) {
    	rbn.set(hash(vehicle.getVehicleId(), hashFile.getHashHeader().getMaxHash()));
    	int rbnStart = -1;
        	Vehicle curr = new Vehicle();
        	int rc = readRec(hashFile, rbn.intValue(), curr);
        	//System.out.println("VR curr vid = " + curr.getVehicleIdAsString());
        	//System.out.println(vehicle.getVehicleIdAsString().equals(curr.getVehicleIdAsString()));
        	if(vehicle.getVehicleIdAsString().equals(curr.getVehicleIdAsString())) {
//        		hashFile.getFile().seek(rbn.intValue());
        		vehicle.fromByteArray(curr.toByteArray());
           	    return ReturnCodes.RC_OK;
        	}
        	else {
           	   	int maxProbe = hashFile.getHashHeader().getMaxProbe();
           	   	rbnStart = rbn.intValue();
       	 		for(int x = rbnStart + 1; x < (maxProbe + rbnStart); x++)
       	 		{
       	 			rbn.set(x);
       		 		curr = new Vehicle();
       		 	    readRec(hashFile, rbn.intValue(), curr);
       	 			if((vehicle.getVehicleIdAsString().equals(curr.getVehicleIdAsString()))) {
       	 				vehicle.fromByteArray(curr.toByteArray());
       	 				return ReturnCodes.RC_OK;
       	 			}	
       	 		}
            }
        return ReturnCodes.RC_REC_NOT_FOUND;
    }
    
    /**
     * vehicleUpdate
     * This function tries to find the given vehicle using its …getVehicleId(). If found, it updates the contents of
     * the vehicle in the hash file. If not found, it returns RC_REC_NOT_FOUND. Note that this function must
     * understand probing.
     * NOTE: You can make your life easier with this function if you use MutableInteger and call some of your
     * other functions to help out.
     * 
     * !!! NEEDS TESTING !!!
     * Testing results:
     * 		
     */
    public static int vehicleUpdate(HashFile hashFile, Vehicle vehicle) {
    	MutableInteger rbn = new MutableInteger(hash(vehicle.getVehicleId(), hashFile.getHashHeader().getMaxHash()));
    	Vehicle curr = new Vehicle();
    	readRec(hashFile, rbn.intValue(), curr);
    	// make for loop similar to other one up further
		// if calculated rbn = actual rbn ie no syns before
		if(vehicle.getVehicleIdAsString().equals(curr.getVehicleIdAsString())) {
			// This is writing correctly
			writeRec(hashFile, rbn.intValue(), vehicle);
			return ReturnCodes.RC_OK;
		}
		// if calculated rbn != acual rbn ie syn before
		else {
			int maxProbe = hashFile.getHashHeader().getMaxProbe();
			for(int x = rbn.intValue() + 1; x < maxProbe; x++) {
				rbn.set(x);
				readRec(hashFile, rbn.intValue(), curr);
	 			if((vehicle.getVehicleIdAsString().equals(curr.getVehicleIdAsString()))) {
	 				writeRec(hashFile, rbn.intValue(), vehicle);
	 				return ReturnCodes.RC_OK;
	 			}
			}
 
    	}
    	return ReturnCodes.RC_REC_NOT_FOUND;
    }
    
    /**
     * vehicleDelete
     * If you did not do the extra credit, create a simple function that just returns RC_NOT_IMPLEMENTED.
     * This function finds the specified vehicle and deletes it by simply setting all bytes in that record to '\0'. 
    */
    public static int vehicleDelete(HashFile hashFile, char [] vehicleId) {
    	// find current VID 
    	// Delete VID
    	// Check if following entries were syn of deleted file
    	// if so move them to the correct new rbn and delete old entry
    	Vehicle vehicle = new Vehicle();
    	
    	MutableInteger rbn = new MutableInteger(1);

    	int maxHash = hashFile.getHashHeader().getMaxHash();
    	for(int x = 1; x <= maxHash; x++)
    	{
    		vehicle = new Vehicle();
    		readRec(hashFile, x, vehicle);
    		//System.out.println("VD x = " + x);
    		//System.out.println("VD vehicle.getVehicleIdAsString() = " + vehicle.getVehicleIdAsString());
    		if(vehicle.getVehicleIdAsString().equals(new String(vehicleId))) {
    			rbn.set(x);
    			x = maxHash + 1;
    		}
    	}
    	
    	vehicle = new Vehicle();
    	writeRec(hashFile,rbn.intValue(), vehicle);
    	
    	return ReturnCodes.RC_OK;
    }
}
