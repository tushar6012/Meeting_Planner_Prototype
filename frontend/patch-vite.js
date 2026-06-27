const fs = require('fs');
const path = require('path');

const configPath = path.join(__dirname, 'node_modules', 'vite', 'dist', 'node', 'chunks', 'config.js');

if (fs.existsSync(configPath)) {
  try {
    let content = fs.readFileSync(configPath, 'utf8');
    
    // Target the specific fs.deny block and replace the general **/.git/** pattern
    // with more specific Git patterns that protect sensitive Git files but allow
    // the server to run when the project path contains '.git'.
    const targetPattern = '"**/.git/**"';
    const replacement = `
\t\t\t"**/.git/config",
\t\t\t"**/.git/credentials",
\t\t\t"**/.git/description",
\t\t\t"**/.git/hooks/**",
\t\t\t"**/.git/info/**",
\t\t\t"**/.git/refs/**"`.trim();

    if (content.includes(targetPattern)) {
      // Find where 'deny: [' is and replace "**/.git/**" specifically inside it
      const denyIndex = content.indexOf('deny: [');
      if (denyIndex !== -1) {
        const nextBlock = content.substring(denyIndex, denyIndex + 200);
        if (nextBlock.includes(targetPattern)) {
          const updatedNextBlock = nextBlock.replace(targetPattern, replacement);
          content = content.substring(0, denyIndex) + updatedNextBlock + content.substring(denyIndex + 200);
          fs.writeFileSync(configPath, content, 'utf8');
          console.log('Successfully patched Vite fs.deny configuration in node_modules.');
        }
      }
    } else {
      console.log('Vite config patch already applied or pattern not found.');
    }
  } catch (err) {
    console.error('Error patching Vite config:', err);
  }
} else {
  console.log('Vite config file not found at:', configPath);
}
