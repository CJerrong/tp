package scrolls.elder.model;

import scrolls.elder.model.person.Person;

import java.util.ArrayList;

import static java.util.Objects.requireNonNull;

public class VersionedAddressBook extends AddressBook {

    private ArrayList<ReadOnlyAddressBook> addressBooks;
    private int addressBookListPointer;
    public VersionedAddressBook(int gid) {
        super(gid);
        this.addressBooks = new ArrayList<ReadOnlyAddressBook>();
        addressBooks.add(new AddressBook(gid));
        this.addressBookListPointer = 0;
    }

    public VersionedAddressBook(ReadOnlyAddressBook toBeCopied) {
        super(toBeCopied);
        this.addressBooks = new ArrayList<ReadOnlyAddressBook>();
        addressBooks.add(new AddressBook(toBeCopied));
        this.addressBookListPointer = 0;
    }

    @Override
    public boolean canUndoAddressBook() {
        if (addressBookListPointer > 0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean canRedoAddressBook() {
        int currSize = addressBooks.size() - 1;

        // Check if pointer is at the latest versioned addressBook
        if (addressBookListPointer < currSize) {
            return true;
        }

        return false;
    }

    @Override
    public void undoAddressBook() throws IllegalStateException {
        if (!canUndoAddressBook()) {
            throw new IllegalStateException("No previous state to undo address book to");
        }

        addressBookListPointer--;
        assert addressBookListPointer >= 0 : "address book list pointer should not be negative";

        this.resetData(addressBooks.get(addressBookListPointer));
    }

    @Override
    public void redoAddressBook() throws IllegalStateException {
        if (!canRedoAddressBook()) {
            throw new IllegalStateException("No subsequent state to redo address book to");
        }

        addressBookListPointer++;

        this.resetData(addressBooks.get(addressBookListPointer));
    }

    @Override
    public void commitAddressBook() throws IllegalStateException {
        if (addressBooks.isEmpty()) {
            throw new IllegalStateException("List of address books within a Versioned Address Book should not be empty");
        }

        AddressBook currentState = new AddressBook(this);
        addressBooks.add(currentState);
        addressBookListPointer++;
    }

    @Override
    public void addPerson(Person p) {
        globalId += 1;
        persons.add(p);

        this.commitAddressBook();
        this.purgeAddressBooks();
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);

        this.commitAddressBook();
        this.purgeAddressBooks();
    }

    @Override
    public void removePerson(Person key) {
        persons.remove(key);

        this.commitAddressBook();
        this.purgeAddressBooks();
    }

    public void purgeAddressBooks() {
        // Call this method each time an operation is done that invalidates previous redo states
        for (int i = addressBookListPointer + 1; i < addressBooks.size(); i++) {
            addressBooks.remove(i);
        }
    }
}
