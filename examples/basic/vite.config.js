import { defineConfig } from 'vite';
import tailwindcss from '@tailwindcss/vite';
import viteCompression from "vite-plugin-compression";
import htma from "@jakobschaefer/htma-vite-plugin"

export default defineConfig({
    plugins: [
        tailwindcss(),
        viteCompression(),
        htma()
    ],
});
