package http;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;

public class URLformatter {
	public static HashSet<String> _blockedPath = new HashSet<>();
    private  String _localPathStr;
    private String _domain;
    private int _port;
    private String _localPath;
    private String _page = "index.html";

    public URLformatter(String fullDomainName) throws URISyntaxException {
        URI uri = new URI(fullDomainName);
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
        if ((!Files.exists(Paths.get(_domain + _localPath + "/" + _page)) || _blockedPath.contains(_localPathStr.substring(0, _localPathStr.lastIndexOf("/")))))
            return false;
        else
            return true;
    }

    public void buildFolderPath() {
        try {
            Files.createDirectories(Paths.get("./" + _domain + _localPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void set_domain(String _domain) {
        this._domain = _domain;
    }

    public String get_localPathStr() {
        return _localPathStr;
    }

    public String get_domain() {
        return _domain;
    }

    public int get_port() {
        return _port;
    }
}
