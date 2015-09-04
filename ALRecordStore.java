package coursework;
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;

/**
 * ALRecordStore is an implementer of the RecordMerchant interface, as part
 * of an example solution to ECM1410 CA2. It uses a single list to manage the stock 
 * and the implementation is limited to 2,147,483,647 reservations in its lifetime
 * If useage would suggest over 2 billion reservations, then a data structure
 * maintaining life reservations could be maintained (however this is excessive
 * for the particular use case).
 * 
 * @author Jonathan Fieldsend 
 * @version 1.0
 */
public class ALRecordStore implements RecordMerchant, Serializable
{
    private ObjectArrayList records = new ObjectArrayList();
    private int nextReservationNumber = 0;
    private int income = 0;
    private int numberInStock = 0;
    private int numberReserved = 0;
    private int numberSold = 0;
    
    /**
     * No argument construtor which initialises an empty ALRecordStore
     */
    public ALRecordStore(){
        
    }
    
    /**
     * @InheritDoc
     */
    public void addRecords(int num, String artist, String title, String id) 
    throws NegativeNumberOfRecordsAddedException, RecordMismatchException,
    IllegalIDException
    {
        // use overloaded addRecords method, rather than duplicate code
        this.addRecords(num, artist, title, "", id);
    }

    /**
     * @InheritDoc
     */
    public void addRecords(int num, String artist, String title,
                           String information, String id)
    throws NegativeNumberOfRecordsAddedException, RecordMismatchException,
    IllegalIDException{
        // check if number added is non-negative
        negativeNumberCheck(num);
        // now check if already contained, and if mismatched, includes legal ID check
        int index = indexOfRecordMatchingID(id);
        Record r = new Record(artist,title,information,id);
        if (index != -1) { // ID found in stock
            if (!records.get(index).equals(r))
                throw new RecordMismatchException("ID exists but other details do not match");
            ((RecordStock) records.get(index)).increaseNumberInStock(num);
        } else {
            // totally new, so add
            records.add(new RecordStock(r,num));
        }
        numberInStock += num;
    }

    /*
     * Helper method which returns the index of the record with matching id in stock.
     * If the id is illegal throws an IllegalIDException. If the record has never been
     * in stock, returns -1.
     */
    private int indexOfRecordMatchingID(String id) throws IllegalIDException {
        legalIDCheck(id);
        assert(id != null);
        for (int i=0; i<records.size(); i++) {
            if (((Record) records.get(i)).getID().equals(id))
                return i;
        }
        return -1;
    }
    
    /*
     * Helper method which provides a number of checks on the legality of an ID
     * and throws an IllegalIDException if any of the checks fail
     */
    private void legalIDCheck(String id) throws IllegalIDException {
        if (id == null) // check we haven't been passed a null reference
            throw new IllegalIDException("ID should not be null");
        if (id.length()!=8) // check it has 8 characters
            throw new IllegalIDException("ID " + id + " is not legal, not 8 characters");
        try { // check it contains a positive hexadecimal
            long val = Long.parseLong(id,16);
            if (val<1) 
                throw new IllegalIDException("ID " + id + " is not legal, not a positive hexadecimal number");
        } catch(NumberFormatException e) {
            throw new IllegalIDException("ID " + id + " is not legal, not a hexadecimal number");
        }
    }
    
    /*
     * Helper method which throws a NegativeNumberOfRecordsAddedException if the argument is
     * negative.
     */
    private void negativeNumberCheck(int num) throws NegativeNumberOfRecordsAddedException {
        if (num < 0)
            throw new NegativeNumberOfRecordsAddedException("Tried to add " + num + " records");
    }
    
    /*
     * Helper method which throws a NegativePriceException if the argument is negative.
     */
    private void negativePriceCheck(int num) throws NegativePriceException {
        if (num < 0)
            throw new NegativePriceException("Tried to set price as " + num + ", negative prices not allowed");
    }
    
    /*
     * Helper method which throws a NegativeNumberOfRecordSoldException if the argument is
     * negative.
     */
    private void negativeSoldCheck(int num) throws NegativeNumberOfRecordsSoldException {
        if (num < 0)
            throw new NegativeNumberOfRecordsSoldException("Tried to sell " + num + " records");
    }
    
    /*
     * Helper method which throws a NegativeNumberOfRecordsReservedException if the argument is
     * negative.
     */
    private void negativeReservedCheck(int num) throws NegativeNumberOfRecordsReservedException {
        if (num < 0)
            throw new NegativeNumberOfRecordsReservedException("Tried to reserve " + num + " records");
    }
    
    /**
     * @InheritDoc
     */
    public void setRecordPrice(String id, int priceInPence) throws
    NegativePriceException, RecordNotInStockException, IllegalIDException
    { 
        // check if price number is legal
        negativePriceCheck(priceInPence);
        assert(priceInPence>-1);
        int index = indexOfRecordMatchingID(id);
        if (index==-1)
            throw new RecordNotInStockException();
        RecordStock r = (RecordStock) records.get(index);    
        if (r.getNumberInStock() == 0)
            throw new RecordNotInStockException(); 
            
        r.setPrice(priceInPence);
    }

    /*
     * Method which returns the RecordStock object corresponding to the id when num of them
     * are required. Checks for exceptional conditions that may occur during the request,
     * and throws relevant exceptions (e.g. RecordNotInStockException, 
     * InsufficientStockException, PriceNotSetException, IllegalIDException).
     */
    private RecordStock checkLegality(int num, String id) throws RecordNotInStockException, 
    InsufficientStockException, PriceNotSetException, IllegalIDException {
        int index = indexOfRecordMatchingID(id); // check id is legal
        assert(id != null); 
        if (index == -1) // check id is of has been in stock
            throw new RecordNotInStockException();
        RecordStock r = (RecordStock) records.get(index);
        assert(r != null); 
        if (r.isPriceNotSet())
            throw new PriceNotSetException();
            
        if (r.getNumberInStock() == 0) // check that id was in stock but is now sold out
            throw new RecordNotInStockException();

        if (r.getNumberAvailableInStock() < num) // check that there are sufficient available
            throw new InsufficientStockException();
        return r;    
    }
    
    /**
     * @InheritDoc
     */
    public void sellRecords(int num, String id) throws RecordNotInStockException, 
    InsufficientStockException, NegativeNumberOfRecordsSoldException, 
    PriceNotSetException, IllegalIDException {
        // check if sold number is legal
        negativeSoldCheck(num);
        // check other legal conditions
        RecordStock r = checkLegality(num,id);    
        // now update attributes used to store running calculations
        income += r.sellStock(num);
        numberInStock -= num;
        numberSold += num;
    }

    /**
     * @InheritDoc
     */
    public int reserveRecords(int num, String id) throws RecordNotInStockException, 
    InsufficientStockException, NegativeNumberOfRecordsReservedException, 
    PriceNotSetException, IllegalIDException { 
        // check if number reserved is legal
        negativeReservedCheck(num);
        // check other legal conditions
        RecordStock r = checkLegality(num,id); 
        // now update attributes used to store running calculations
        r.reserve(num,nextReservationNumber);
        numberReserved += num;
        return nextReservationNumber++; //return then increment reservation number
    }
    
    /*
     * Removes reservationNumber from stock and returns references to correcsponding Reservation 
     * object. If the rservationNumber does not exist in the stock, returns null.
     */
    private Reservation removeReservationObject(int reservationNumber){
        for (int i=0; i< records.size(); i++){
            RecordStock r = ((RecordStock) records.get(i)); // check each RecordStock in turn
            Reservation res = r.unreserve(reservationNumber); // see if reservationNumber used
            if (res!=null)
                return res;
        }
        return null;
    }
    
    /**
     * @InheritDoc
     */
    public void unreserveRecords(int reservationNumber)
    throws ReservationNumberNotRecognisedException {
        Reservation res = removeReservationObject(reservationNumber);
        if (res == null)
            throw new ReservationNumberNotRecognisedException();
        // update tracked value of total reserved number
        numberReserved -= res.getNumberReserved();
    }
    
    /**
     * @InheritDoc
     */
    public void sellRecords(int reservationNumber)
    throws ReservationNumberNotRecognisedException { 
        Reservation res = removeReservationObject(reservationNumber); // get corresponding Reservation object
        if (res==null)
            throw new ReservationNumberNotRecognisedException();
        // sell at the best price to the customer
        income += res.getRecordStock().sellStock(res.getNumberReserved(),res.getReservedRecordPrice());
        numberReserved -= res.getNumberReserved();
        numberInStock -= res.getNumberReserved();
        numberSold += res.getNumberReserved();
    }

    /**
     * @InheritDoc
     */
    public int recordsInStock(){ return numberInStock; }

    /**
     * @InheritDoc
     */
    public int reservedRecordsInStock(){ return numberReserved; }

    /**
     * @InheritDoc
     */
    public int recordsInStock(String id) throws IllegalIDException{ 
        int index = indexOfRecordMatchingID(id);
        if (index == -1)
            return 0;
        return ((RecordStock) records.get(index)).getNumberInStock();
    } 

    /**
     * @InheritDoc
     */
    public void saveMerchantContents(String filename) throws IOException {
        ObjectOutputStream objectOutput = new ObjectOutputStream(new FileOutputStream(filename));
        objectOutput.writeObject(this);
        objectOutput.close();
    }
    
    /**
     * @InheritDoc
     */
    public void loadMerchantContents(String filename) throws IOException, 
    ClassNotFoundException {
        ObjectInputStream objectInput = new ObjectInputStream(new FileInputStream(filename));
        ALRecordStore temp = (ALRecordStore) objectInput.readObject();
        objectInput.close();
        this.records = temp.records;
        this.nextReservationNumber = temp.nextReservationNumber;
        this.income = temp.income;
        this.numberInStock = temp.numberInStock;
        this.numberSold = temp.numberSold;
        this.numberReserved = temp.numberReserved;
    }
    
    /**
     * @InheritDoc
     */
    public int getNumberOfDifferentRecordsInStock() { 
        int num = records.size();
        // compenstate for those that have been in stock but are currently sold out
        for (int i=0; i<records.size(); i++)
            if (((RecordStock) records.get(i)).getNumberInStock() == 0)
                num--;
        return num;
    }

    /**
     * @InheritDoc
     */
    public int getNumberOfSoldRecords() { return numberSold; }

    /**
     * @InheritDoc
     */
    public int getNumberOfSoldRecords(String id) throws IllegalIDException { 
        int index = indexOfRecordMatchingID(id);
        if (index==-1)
            return 0;
        return ((RecordStock) records.get(index)).getNumberSold();
    }

    /**
     * @InheritDoc
     */
   public int getTotalPriceOfSoldRecords() { return income; }

    /**
     * @InheritDoc
     */
    public int getTotalPriceOfSoldRecords(String id) throws IllegalIDException { 
        int index = indexOfRecordMatchingID(id);
        if (index == -1) 
            return 0;
        return ((RecordStock) records.get(index)).getPriceOfSold(); 
    }

    /**
     * @InheritDoc
     */ 
    public int getTotalPriceOfReservedRecords() { 
        int num = 0;
        for (int i = 0; i < records.size(); i++)
            num += ((RecordStock) records.get(i)).getTotalPriceOfReserved();
        return num;
    }

    /**
     * 
     */
    public String getRecordDetails(String id) throws IllegalIDException { 
        int index = indexOfRecordMatchingID(id);
        if (index == -1)
            return "";
        return ((RecordStock) records.get(index)).getInformation(); 
    }

    /**
     * @InheritDoc
     */
    public void empty() {
        // reset state
        records = new ObjectArrayList();
        nextReservationNumber = 0;
        income = 0;
        numberInStock = 0;
        numberReserved = 0;
        numberSold = 0;
    }

    /**
     * @InheritDoc
     */
    public void resetSaleAndCostTracking() { 
        // just reset sale and cost tracking state members
        numberSold = 0;
        income = 0;
        for (int i=0; i<records.size(); i++)
            ((RecordStock) records.get(i)).resetSaleAndCostTracking(); 
    }
}
