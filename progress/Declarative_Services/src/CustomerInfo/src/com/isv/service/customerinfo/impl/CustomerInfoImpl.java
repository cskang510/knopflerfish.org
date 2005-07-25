public class CustomerInfoImpl implements CustomerInfo { 
   public Customer getCustomer(String customerID) { 
       // here is where the interaction with business logic would go 
       Customer cust = new Customer(); 
       cust.setCustNo(customerID); 
       cust.setFirstName("Victor"); 
       cust.setLastName("Hugo"); cust.setSymbol("IBM"); 
       cust.setNumShares(100); cust.setPostalCode("10589"); 
       cust.setErrorMsg(""); return cust; 
   } 
}