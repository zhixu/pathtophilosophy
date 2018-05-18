function getPath() {
    // get link value without spaces
    let link = document.getElementById("url").value.replace(/\s/g, '');
    
    // if it's not a Wikipedia link, display error
    if (!link.includes("wikipedia.org/wiki/")) {
        console.log("link does not include wiki part");
        document.getElementById("error").setAttribute("style", "display: visible;");
        return;
    }
    
    // if there's no protocol, add one
    if (link.indexOf("http://") == -1 && link.indexOf("https://") == -1) {
        link = "https://" + link;
    }
    
    document.getElementById("error").setAttribute("style", "display: none;");
    sendRequest(link);
}

function getRandom() {
    sendRequest("https://en.wikipedia.org/wiki/Special:Random");
}

function sendRequest(url) {
    let xhr = new XMLHttpRequest();
    let queryParam = "?url=" + encodeURI(url);//encodeURI(document.getElementById("url").value);
    xhr.open('GET', 'webresources/paths' + queryParam);
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    xhr.onload = function() {
        if (xhr.status === 200) {
            showProcessedView();
            processResponse(xhr.responseText);
        }
        else if (xhr.status !== 200) {
            showProcessedView();
            processError(xhr.status);
        }
    };
    xhr.send();
    showLoadingView();
};

function showLoadingView() {
    document.getElementById("solution").setAttribute("style", "display: none;");
    document.getElementById("loading").setAttribute("style", "display: visible;");
}

function showProcessedView() {
    document.getElementById("solution").setAttribute("style", "display: visible;");
    document.getElementById("loading").setAttribute("style", "display: none;");
}

function processResponse(xhrResponse) {
    // clear out previous response if any
    let pathBlock = document.getElementById('path');
    while (pathBlock.firstChild) {
        pathBlock.removeChild(pathBlock.firstChild);
    }
    
    let json = JSON.parse(xhrResponse);
    document.getElementById('message').innerHTML = json.message;

    let path = json.path;
    for (let i = 0; i < path.length-1; i++) {
        createArticleDisplay(path[i], "arrow");
    }
    
    let lastTitle = path[path.length - 1]
    if (lastTitle === "Philosophy") {
        createArticleDisplay(path[path.length - 1], "check");
    } else {
        createArticleDisplay(path[path.length - 1], "cross");
    }
}

function createArticleDisplay(name, cssClass) {
        let element = document.createElement("div");
        element.className = "article-block";
        
        let pathName = document.createElement("div");
        pathName.innerHTML = name;
        element.appendChild(pathName);
        
        let icon = document.createElement("div");
        icon.className = cssClass;
        icon.classList.add("symbol");
        element.appendChild(icon);
        
        if (cssClass == "cross") element.classList.add("red-block");
        else if (cssClass == "check") element.classList.add("green-block"); 
        else if (cssClass == "arrow") element.classList.add("blue-block")

        let pathBlock = document.getElementById('path');
        pathBlock.appendChild(element);
}

function processError(status) {
    alert('Request failed.  Returned status of ' + status);
}