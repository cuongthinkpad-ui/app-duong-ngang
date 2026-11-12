import {readFile,rm,mkdir,cp} from 'node:fs/promises';
import {fileURLToPath} from 'node:url';
process.chdir(fileURLToPath(new URL('..',import.meta.url)));
const manifest=JSON.parse(await readFile('public/manifest.webmanifest','utf8'));
if(manifest.display!=='standalone')throw Error('PWA must be standalone');
await rm('dist',{recursive:true,force:true});await mkdir('dist');await cp('public','dist',{recursive:true});
console.log('Static shell built. Production: build and deploy Dockerfile with the backend and PostgreSQL.');
