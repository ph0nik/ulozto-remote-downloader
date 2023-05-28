package service;

import model.DownloadElement;

import java.util.List;

public interface StateService {

    /*
    * Removes all elements marked as finished from the list.
    * */
    public StateService removeFinishedElements();

    public StateService removeInvalidElements();

    /*
    * Removes element with given element index.
    * */
    public List<DownloadElement> removeSelectedElement(int i);

    /*
     * Check if state file exist, if so return true, otherwise return false
     * */
    public boolean isStateFile();

    /*
     * Load state list from a file
     * */
    public List<DownloadElement> getStateList();

    /*
    * Adds given element to state list and saves it to disk.
    * */
    public void saveState(DownloadElement downloadElement);

    void saveState();
}
