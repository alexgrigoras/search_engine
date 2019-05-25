package crawler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

/**
 * URL Formatting class
 * @author alex_
 * 
 */
public class URLformatter {
	public static HashSet<String> _blockedPath = new HashSet<>();
	private String _fullDomainName;
    private String _localPathStr;
    private String _scheme;
    private String _domain;
    private int _port;
    private String _localPath;
    private String _resource = "index.html";
    private String _filesFolder = "files/html/";

    /**
     * Class constructor
     * @param fullDomainName: URL
     * @throws URISyntaxException
     */
    public URLformatter(String fullDomainName) throws URISyntaxException {
    	_fullDomainName = fullDomainName;
        URI uri = new URI(fullDomainName);
        _scheme = uri.getScheme() + "/";
        _domain = uri.getHost();
        _domain = _domain.startsWith("www.") ? _domain.substring(4) : _domain;

        _port = (uri.getPort() == -1 ? 80 : uri.getPort());
        _localPathStr = uri.getPath();
        _localPath = _localPathStr;
        if (_localPathStr.contains(".")) {
            _localPath = _localPathStr.substring(0,
                    _localPathStr.lastIndexOf("/"));
            _resource = _localPathStr.substring(_localPathStr.lastIndexOf("/"));
        }
    }

    /**
     * Get the local path
     * @return
     */
    public String getLocalPath() {
        return _localPath;
    }

    /**
     * Set the local path
     * @param _localPath
     */
    public void setLocalPath(String _localPath) {
        this._localPath = _localPath;
    }

    /**
     * Get the resource
     * @return
     */
    public String getResource() {
        return _resource;
    }

    /**
     * Set the resource
     * @param _page
     */
    public void setResource(String _page) {
        this._resource = _page;
    }

    /**
     * Check if link was already processed
     * @return
     */
    public boolean wasProcessed() {
        if ((!Files.exists(Paths.get(_filesFolder + _scheme + _domain + _localPath + _resource)) || _blockedPath.contains(_localPathStr.substring(0, _localPathStr.lastIndexOf("/"))))) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Create the folder path for storing the resources
     */
    public void buildFolderPath() {
        try {
            Files.createDirectories(Paths.get("./" + _filesFolder + _scheme + _domain + _localPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the complete URL
     * @return
     */
    public String getFullDomainName() {
    	return _fullDomainName;
    }
    
    /**
     * Get the folder where the files are stored
     * @return
     */
    public String getFilesFolder() {
    	return _filesFolder;
    }
    
    /**
     * Get the complete local path
     * @return
     */
    public String getLocalPathStr() {
        return _localPathStr;
    }

    /**
     * Get the domain
     * @return
     */
    public String getDomain() {
        return _domain;
    }

    /**
     * Set the domain
     * @param _domain
     */
    public void setDomain(String _domain) {
        this._domain = _domain;
    }
    
    /**
     * Get the scheme
     * @return
     */
    public String getScheme() {
    	return _scheme;
    }
    
    /**
     * Get the port
     * @return
     */
    public int getPort() {
        return _port;
    }
}
