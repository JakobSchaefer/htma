import path from "path"
import { promises as fs } from "fs"
import { defineConfig } from 'vite';
import tailwindcss from '@tailwindcss/vite';
import viteCompression from "vite-plugin-compression";

function htma() {
    const pluginConfig = {
        componentNames: [],
        componentPaths: {},
        componentNamesByPath: {}
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
                    outDir: path.resolve(__dirname, "dist"),
                    rollupOptions: {
                        input: path.resolve(__dirname, "web", "__root.js"),
                    }
                }
            }
        },
        async configResolved() {
            const componentsDir = path.resolve(__dirname, "web/__components")
            const components = await fs.readdir(componentsDir)
            for (const componentFile of components) {
                if (componentFile.endsWith(".html")) {
                    const componentName = componentFile.substring(0, componentFile.length - ".html".length)
                    const componentPath = path.join(componentsDir, componentFile)
                    pluginConfig.componentNames.push(componentName)
                    pluginConfig.componentPaths[componentName] = componentPath
                    pluginConfig.componentNamesByPath[componentPath] = componentName
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
                const componentFilePath = pluginConfig.componentPaths[componentName]
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
                const componentImportStatements = pluginConfig.componentNames.map((it) => `import "virtual:htma/${it}"`)
                    .join("\n")
                return `${componentImportStatements}\n${src}`
            } else {
                return src
            }
        },
        configureServer(server) {
            for (const componentName of pluginConfig.componentNames) {
                server.watcher.add(pluginConfig.componentPaths[componentName])
            }

            server.watcher.on("change", (file) => {
                const componentName = pluginConfig.componentNamesByPath[file]
                if (componentName) {
                    const module = server.moduleGraph.getModuleById('\0' + "virtual:htma/" + componentName)
                    server.moduleGraph.invalidateModule(module)
                }
            })
        }
    }
}

export default defineConfig({
    plugins: [
        tailwindcss(),
        viteCompression(),
        htma()
    ],
});
