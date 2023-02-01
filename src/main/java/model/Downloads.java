package model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;

@XmlRootElement(name = "downloadList")
@XmlAccessorType(XmlAccessType.FIELD)
public class Downloads {

    @XmlElement(name = "element")
    private List<DownloadElement> downloadElements = new LinkedList<>();

    public List<DownloadElement> getDownloadElements() {
        return downloadElements;
    }

    public void setDownloadElements(List<DownloadElement> list) {
        this.downloadElements = list;
    }

}
