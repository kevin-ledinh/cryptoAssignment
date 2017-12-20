import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TxHandler {

	private UTXOPool _utxoPool;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
    	_utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
    	if( _utxoPool == null ) {
    		return false;
    	}
    	
    	if( tx == null ) {
    		return false;
    	}
    	
    	ArrayList<Transaction.Input> inputList = tx.getInputs();
    	if( inputList == null ) {
    		return false;
    	} else {
    		/* implement (1) */
    		for( Transaction.Input ip : inputList ) {
    			if( ! _utxoPool.contains( new UTXO( ip.prevTxHash, ip.outputIndex ) ) ) {
    				System.out.println("implement (1) false");
    				return false;
    			}
    		}
    	}
    	
    	/* implement (2) */
    	for( int i = 0; i < tx.numInputs(); i++ ) {
    		if( ! Crypto.verifySignature( _utxoPool.getTxOutput(new UTXO ( inputList.get(i).prevTxHash, inputList.get(i).outputIndex )).address, 
    									  tx.getRawDataToSign( i ), 
    									  inputList.get(i).signature) ) {
				System.out.println("implement (2) false");
    			return false;
    		}
    	}
    	
    	/* implement (3) */
    	List<UTXO> UTXOlist = new ArrayList<UTXO>();
    	for( Transaction.Input ip : inputList ) {
        	UTXOlist.add(new UTXO(ip.prevTxHash, ip.outputIndex));
		}
    	Set<UTXO> set = new HashSet<UTXO>(UTXOlist);
    	if( set.size() < UTXOlist.size() ) {
			System.out.println("implement (3) false");
    		return false;
    	}
    	
    	/* implement (4) */
    	ArrayList< Transaction.Output > outputList = tx.getOutputs();
    	for( Transaction.Output op : outputList ) {
    		if( op.value < 0 ) {
				System.out.println("implement (4) false");
    			return false;
    		}
    	}
    	
    	/* implement (5) */
    	double outputSum = 0;
    	double inputSum = 0;    	
    	for( Transaction.Output op : outputList ) {
    		outputSum += op.value; // This is the sum of all outputs from the current transaction
    	}
    	
    	for(Transaction.Input ip : inputList) {
    		// Calculate the sum of all inputs from the current transactions
    		// These inputs originate from some outputs in the UTXO Pool 
    		inputSum += _utxoPool.getTxOutput(new UTXO(ip.prevTxHash, ip.outputIndex)).value;
		}
    	
    	
    	if( outputSum > inputSum ) {
			System.out.println("implement (5) false");
    		return false;
    	}
    	
    	return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
    	ArrayList<Transaction> validTxList = new ArrayList<Transaction>();
    	int returnListSize = 0;
    	
    	/* Update the current UTXO Pool */
		for( int i = 0 ; i < possibleTxs.length ; i ++ ) {
			
			/* record the size of the valid tx List at the beginning of the loop */
			if( i == 0 ) {
				returnListSize = validTxList.size();
			}
			
			if( isValidTx( possibleTxs[ i ] ) ) {
    			validTxList.add( possibleTxs[ i ] );
    			
				for( Transaction.Input ip : possibleTxs[ i ].getInputs() ) {
					_utxoPool.removeUTXO( new UTXO( ip.prevTxHash , ip.outputIndex ) );
				}
				
				possibleTxs[ i ].finalize();
				for( int y = 0 ; y < possibleTxs[ i ].numOutputs() ; y ++ ) {
					_utxoPool.addUTXO( new UTXO( possibleTxs[ i ].getHash() , y ) , possibleTxs[ i ].getOutput( y ) );
					
				}
				
				possibleTxs[ i ] = null;
			}
			
			/* record the size of the valid tx List at the end of the loop */
			if( i == ( possibleTxs.length - 1 ) ) {
				/* if there are new tx added into the valid tx list, we loop again */
				if( validTxList.size() > returnListSize ) {
					returnListSize = validTxList.size();
					i = -1;
				} // else we do nothing and exit the loop
			}
		}
    	
    	return validTxList.toArray( new Transaction[ validTxList.size() ] );
    }

}
