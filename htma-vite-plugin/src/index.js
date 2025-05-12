import path from "path"
import fs from "fs/promises"

export default function htma() {
    let projectDir = ""
    const pluginConfig = {
        webDir: "web",
        assets: [],
        webComponents: [] // [{ componentName: "my-component", filePath: "components/(my-component)/my-component.html}]
    }
    return {
        name: "htma",
        config() {
            return {
                server: {
                    host: "127.0.0.1",
                    port: 5174,
                    origin: "http://localhost:5174",
                },
                build: {
                    manifest: true,
                    outDir: path.resolve(projectDir, "dist"),
                    rollupOptions: {
                        input: path.resolve(projectDir, "web", "__root.js"),
                    }
                }
            }
        },
        async configResolved(cfg) {
            // detect assets
            projectDir = cfg.root
            const webDir = path.resolve(projectDir, pluginConfig.webDir)
            const files = await fs.readdir(webDir, { recursive: true })
            pluginConfig.assets = files.filter((it) => it.endsWith(".png"))
                .map(it => it.replaceAll(path.sep, "/"))

            // detect web components
            const componentRegex = /\(([^)]+)\)\/\1.html/
            for (const file of files) {
                if (componentRegex.test(file)) {
                    const componentName = componentRegex.exec(file)[1]
                    const filePath = `${pluginConfig.webDir}/${file}`
                    console.log(`Found web component: ${componentName} at ${filePath}`)
                    pluginConfig.webComponents.push({
                        componentName,
                        filePath
                    })
                } else {
                }
            }
        },
        async resolveId(id) {
            if (id.startsWith("virtual:htma/")) {
                return '\0' + id
            }
        },
        async load(id) {
            const prefix = '\0' + "virtual:htma/"
            if (id.startsWith(prefix)) {
                const componentName = id.substring(prefix.length)
                const componentFilePath = pluginConfig.webComponents.find(it => it.componentName === componentName).filePath
                const content = await fs.readFile(componentFilePath, 'utf-8')
                const scriptMatch = content.match(/<script>([\s\S]*?)<\/script>/)
                if (scriptMatch) {
                    const scriptContent = scriptMatch[1]
                    return scriptContent
                } else {
                    return ""
                }
            }
        },
        async transform(src, id) {
            if (id.endsWith("__root.js")) {
                const assetImportStatements = pluginConfig.assets.map(it => `import "./${it}"`)
                    .join("\n")
                const componentImportStatements = pluginConfig.webComponents.map((it) => `import "virtual:htma/${it.componentName}"`)
                    .join("\n")
                return `${assetImportStatements}\n${componentImportStatements}\n${src}`
            } else {
                return src
            }
        },
        configureServer(server) {
            for (const webComponent of pluginConfig.webComponents) {
                server.watcher.add(webComponent.filePath)
            }

            server.watcher.on("change", (file) => {
                console.log(`File changed: ${file}`)
                const componentName = pluginConfig.webComponents.find(it => it.filePath === file)?.componentName
                if (componentName) {
                    const module = server.moduleGraph.getModuleById('\0' + "virtual:htma/" + componentName)
                    server.moduleGraph.invalidateModule(module)
                }
            })
        }
    }
}

