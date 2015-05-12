import groovy.io.FileType

def startServer = { config, index ->
    def cmd = config.basePath + config.serverPaths[index].script + " " + config.basePath + config.serverPaths[index].server
    // Using ".text" will wait until server is down
    //def res = cmd.execute().text

    cmd.execute()
    return "OK"
}

def config = new ServerConfig().load()

def thisScript = "/runmc.groovy"

if (!session) {
    session = request.getSession(true)
}

if (session.admin == null) {
    session.admin = false
}

def cmd = request.getParameter("cmd")

if (cmd == "start") {
    startServer(config, request.getParameter("index").toInteger())
    redirect(thisScript)
}

if (cmd == "login") {
    if (request.getParameter("username") == config.auth.username
            && request.getParameter("password") == config.auth.password) {
        session.admin = true
        session.username = config.auth.username
    }
    redirect(thisScript)
}

if (cmd == "logout") {
    session.admin = null
    redirect(thisScript)
}

ProcessList pl = new ProcessList()
def ps = pl.getCurrent()

html.html {
    head {
        title("Minecraft Server Web Launcher")

        link(href: "//maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css", rel: "stylesheet")
    }
    body {
        div(class: "container") {
            div(class: "row") {
                div(class: "col-sm-12 col-mg-12 col-lg-12") {

                    div(class: "panel panel-default") {
                        div(class: "panel-heading") {
                            yeild "Minecraft Server Launcher"
                        }
                        table(class: "table") {
                            tr {
                                td {
                                    if (session.admin == true) {
                                        yield "Welcome, ${session.username}!"
                                        a(class: "button btn btn-primary", href: thisScript + "?cmd=logout", "Logout")
                                    } else {
                                        form(role: "form", class: "form-inline", name: "loginForm", method: "POST", action: thisScript) {
                                            input(type: "hidden", name: "cmd", value: "login")
                                            div(class: "form-group") {
                                                input(class: "form-control", type: "text", name: "username", placeholder: "Enter username")
                                            }
                                            div(class: "form-group") {
                                                input(class: "form-control", type: "password", name: "password", placeholder: "Enter password")
                                            }
                                            button(class: "button btn btn-primary", onclick: "document.loginForm.submit()", "Login")
                                        }
                                    }
                                }
                            } // tr
                        } // table
                    } // div panel
                    div(class: "panel panel-default") {
                        div(class: "panel-heading") {
                            button(type: "button", class: "btn btn-default", onclick: "document.location='" + thisScript + "'") {
                                span(class: "glyphicon glyphicon-refresh")
                                yeild "Refresh"
                            }
                            yeild "Minecraft Server List"
                        } // div .panel-heading
                        table(class: "table") {
                            thead {
                                tr {
                                    td { yield "Actions" }
                                    td { yield "Status" }
                                    td { yield "Server Name" }
                                    td { yield "Port#" }
                                    td { yield "Server Description" }
                                    td { yield "Minecraft Version" }
                                    td { yield "Installed Mods" }
                                    td { yield "Required Resource Packs/Map files" }
                                } // tr
                            } // thead
                            config.serverPaths.eachWithIndex { server, i ->
                                def status = pl.findCommand(ps, server.server)
                                tr {
                                    td {
                                        if (session.admin == true) {
                                            button(class: "button btn btn-primary", onclick: "location.href='" + thisScript + "?cmd=start&index=" + i + "'", "Start")
                                        } else {
                                            button(class: "button btn btn-disabled", "Start")
                                        }
                                    } // td
                                    td {
                                        span(class: status == true ? "btn btn-success" : "btn btn-danger", status == true ? "Running" : "Stopped")
                                    }
                                    td { yield "$server.name" }
                                    td { yield "$server.port" }
                                    td { yield "$server.description" }
                                    td { yield "$server.minecraftVersion" }
                                    td {
                                        try {
                                            File dir = new File(config.basePath.toString() + server.server.toString() + "/mods")
                                            dir.eachFile(FileType.FILES) { file ->
                                                p {
                                                    a(class: "glyphicon glyphicon-download", href: config.download.baseUrl + file.getName(), file.getName())
                                                }
                                            }
                                        } catch (FileNotFoundException) {
                                            // ignore this error
                                        }
                                    } // td
                                    td {
                                        server.requiredResourcePacks.each { rp ->
                                            p {
                                                a(class: "glyphicon glyphicon-download", href: config.download.baseUrl + rp.name, rp.name)
                                                br {}
                                                a(class: "glyphicon glyphicon-new-window", href: rp.url, target: "_blank", "Source")
                                            }
                                        }
                                    } // td
                                } // tr
                            } // each
                        } // table
                    }// div panel
                } // div .col
            } // div .row
        } // div .ccontainer
    } // body
} // html