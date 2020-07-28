package natrodrigues.natbank.server.services;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import natrodrigues.natbank.server.config.exception.TransactionException;
import natrodrigues.natbank.server.form.TransactionForm;
import natrodrigues.natbank.server.models.Transaction;
import natrodrigues.natbank.server.repository.TransactionRepository;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

	public void verify(@Valid TransactionForm transactionForm) throws TransactionException {
        String id = transactionForm.getUuid();
        Optional<Transaction> transaction = transactionRepository.findById(id);
        if(transaction.isPresent()) {
            throw new TransactionException("uuid", "Trasaction already exists");
        }
	}

}
