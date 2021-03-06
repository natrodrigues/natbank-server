package natrodrigues.natbank.server.controllers;

import javax.transaction.Transactional;
import javax.validation.Valid;

import natrodrigues.natbank.server.models.Contact;
import natrodrigues.natbank.server.services.Services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import natrodrigues.natbank.server.config.exception.FormError;
import natrodrigues.natbank.server.config.exception.NatbankException;
import natrodrigues.natbank.server.controllers.form.AccountForm;
import natrodrigues.natbank.server.controllers.form.TransactionForm;
import natrodrigues.natbank.server.models.Account;
import natrodrigues.natbank.server.models.Transaction;
import natrodrigues.natbank.server.models.TransactionType;
import natrodrigues.natbank.server.controllers.repository.AccountRepository;
import natrodrigues.natbank.server.controllers.repository.TransactionRepository;

@RestController
@RequestMapping("/transactions")
public class TransactionsController {

    @Autowired
    private Services<TransactionForm> transactionService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private Services<AccountForm> accountService;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private Services<Contact> contactServices;

    @PostMapping
    @Transactional
    public ResponseEntity<Object> newTransaction(@RequestBody @Valid TransactionForm transactionForm, Errors errors)
            throws NatbankException {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(FormError.getErrorList(errors));
        }
        transactionService.verify(transactionForm);
        accountService.verify(transactionForm.getAccountForm());
        contactServices.verify(transactionForm.getContact());

        Transaction senderTransaction = transactionForm.convert(TransactionType.SEND);
        senderTransaction.setContact(transactionForm.getContact());
        Transaction recieverTransaction = transactionForm.convert(TransactionType.RECIEVE);
        recieverTransaction.setContact(transactionForm.getAccountForm().toContact());

        Account senderAccount = transactionForm.getAccountForm().convert(accountRepository);
        Account recieverAccount = accountRepository.findByNumber(senderTransaction.getContact().getAccountNumber())
                .get();

        senderAccount.addTransaction(senderTransaction, transactionRepository);
        recieverAccount.addTransaction(recieverTransaction, transactionRepository);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<?> getAllTransactions(@RequestBody @Valid AccountForm accountForm, Errors errors)
            throws NatbankException {
        if(errors.hasErrors()) {
            return ResponseEntity.badRequest().body(FormError.getErrorList(errors));
        }
        accountService.verify(accountForm);
        Account account = accountRepository.findByNumber(accountForm.getNumber()).get();

        return ResponseEntity.ok().body(account.getTransactions());
    }

}