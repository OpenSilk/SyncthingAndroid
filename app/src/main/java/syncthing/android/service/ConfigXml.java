package syncthing.android.service;

import android.content.Context;
import android.os.Environment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import timber.log.Timber;

/**
 * Provides direct access to the config.xml file in the file system.
 *
 * This class should only be used if the syncthing API is not available (usually during startup).
 */
public class ConfigXml {

    public static final String CONFIG_FILE = "config.xml";

    private File mConfigFile;

    private Document mConfig;

    private ConfigXml(File configFile) {
        mConfigFile = configFile;
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            mConfig = db.parse(mConfigFile);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ConfigXml get(Context context) {
        File configfile = getConfigFile(context);
        if (!configfile.exists()) {
            return null;
        } else {
            return new ConfigXml(configfile);
        }
    }

    public static File getConfigFile(Context context) {
        return new File(context.getFilesDir(), CONFIG_FILE);
    }

    /**
     * Updates the config file.
     * <p/>
     * Coming from 0.2.0 and earlier, globalAnnounceServer value "announce.syncthing.net:22025" is
     * replaced with "194.126.249.5:22025" (as domain resolve is broken).
     * <p/>
     * Coming from 0.3.0 and earlier, the ignorePerms flag is set to true on every folder.
     */
    public void updateIfNeeded() {
        Timber.d("Checking for needed config updates");
        boolean changed = false;
        Element options = (Element) mConfig.getDocumentElement()
                .getElementsByTagName("options").item(0);
        Element gui = (Element) mConfig.getDocumentElement()
                .getElementsByTagName("gui").item(0);

        // Hardcode default globalAnnounceServer ip.
        Element globalAnnounceServer = (Element)
                options.getElementsByTagName("globalAnnounceServer").item(0);
        if (globalAnnounceServer.getTextContent().equals("udp4://announce.syncthing.net:22026")) {
            Timber.d("Replacing globalAnnounceServer host with ip");
            globalAnnounceServer.setTextContent("udp4://194.126.249.5:22026");
            changed = true;
        }

        NodeList folders = mConfig.getDocumentElement().getElementsByTagName("folder");
        for (int i = 0; i < folders.getLength(); i++) {
            Element r = (Element) folders.item(i);
            // Set ignorePerms attribute.
            if (!r.hasAttribute("ignorePerms") ||
                    !Boolean.parseBoolean(r.getAttribute("ignorePerms"))) {
                Timber.d("Set 'ignorePerms' on folder " + r.getAttribute("id"));
                r.setAttribute("ignorePerms", Boolean.toString(true));
                changed = true;
            }

            if (applyLenientMTimes(r)) {
                changed = true;
            }
        }

        if (changed) {
            saveChanges();
        }
    }

    /**
     * Set 'lenientMtimes' (see https://github.com/syncthing/syncthing/issues/831) on the
     * given folder.
     *
     * @return True if the XML was changed.
     */
    private boolean applyLenientMTimes(Element folder) {
        NodeList childs = folder.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node item = childs.item(i);
            if (item.getNodeName().equals("lenientMtimes")) {
                if (item.getTextContent().equals(Boolean.toString(false))) {
                    item.setTextContent(Boolean.toString(true));
                    return true;
                }
                return false;
            }
        }

        // XML tag does not exist, create it.
        Timber.d("Set 'lenientMtimes' on folder " + folder.getAttribute("id"));
        Element newElem = mConfig.createElement("lenientMtimes");
        newElem.setTextContent(Boolean.toString(true));
        folder.appendChild(newElem);
        return true;
    }

    /**
     * Change default folder id to camera and path to camera folder path.
     */
    public void changeDefaultFolder() {
        Element folder = (Element) mConfig.getDocumentElement()
                .getElementsByTagName("folder").item(0);
        folder.setAttribute("id", "camera");
        folder.setAttribute("path", Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
        folder.setAttribute("ro", "true");
        saveChanges();
    }

    /**
     * Writes updated mConfig back to file.
     */
    private void saveChanges() {
        try {
            Timber.d("Writing updated config back to file");
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(mConfig);
            StreamResult streamResult = new StreamResult(mConfigFile);
            transformer.transform(domSource, streamResult);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

}
