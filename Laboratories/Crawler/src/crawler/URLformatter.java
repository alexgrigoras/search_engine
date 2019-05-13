package crawler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

/**
 * URL Formatting class
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
    private String _page = "index.html";
    private String _filesFolder = "files/html/";

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
            _page = _localPathStr.substring(_localPathStr.lastIndexOf("/"));
        }
    }

    public String get_localPath() {
        return _localPath;
    }

    public void set_localPath(String _localPath) {
        this._localPath = _localPath;
    }

    public String get_page() {
        return _page;
    }

    public void set_page(String _page) {
        this._page = _page;
    }

    public boolean wasProcessed() {
        if ((!Files.exists(Paths.get(_filesFolder + _scheme + _domain + _localPath + _page)) || _blockedPath.contains(_localPathStr.substring(0, _localPathStr.lastIndexOf("/"))))) {
            return false;
        }
        else {
            return true;
        }
    }

    public void buildFolderPath() {
        try {
            Files.createDirectories(Paths.get("./" + _filesFolder + _scheme + _domain + _localPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void set_domain(String _domain) {
        this._domain = _domain;
    }

    public String get_fullDomainName() {
    	return _fullDomainName;
    }
    
    public String get_filesFolder() {
    	return _filesFolder;
    }
    
    public String get_localPathStr() {
        return _localPathStr;
    }

    public String get_domain() {
        return _domain;
    }

    public String get_scheme() {
    	return _scheme;
    }
    
    public int get_port() {
        return _port;
    }
}
