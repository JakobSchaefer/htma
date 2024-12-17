import path from "path"
import { defineConfig } from 'vite';
import tailwindcss from '@tailwindcss/vite';
import viteCompression from "vite-plugin-compression";

export default defineConfig({
    plugins: [
        tailwindcss(),
        viteCompression()
    ],
    server: {
        host: "127.0.0.1",
        port: 5173,
        origin: "http://localhost:5173",
    },
    build: {
        manifest: true,
        outDir: path.resolve(__dirname, "dist"),
        rollupOptions: {
            input: path.resolve(__dirname, "web", "main.js"),
        },
    },
});
