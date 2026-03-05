const fs = require('fs');
const path = require('path');

function walkDir(dir) {
    const files = [];
    const list = fs.readdirSync(dir);
    list.forEach((file) => {
        const fullPath = path.join(dir, file);
        const stat = fs.statSync(fullPath);
        if (stat && stat.isDirectory()) {
            files.push(...walkDir(fullPath));
        } else if (file.endsWith('.vue') || file.endsWith('.js')) {
            files.push(fullPath);
        }
    });
    return files;
}

const srcPath = path.join(__dirname, 'src');
const files = walkDir(srcPath);

let modifiedCount = 0;

files.forEach((file) => {
    let content = fs.readFileSync(file, 'utf8');
    const originalContent = content;
    
    content = content.replace(/@\/components\/widget\/EmojiPicker\.vue/g, '@/components/common/EmojiPicker.vue');
    content = content.replace(/@\/components\/widget\/([A-Za-z0-9_-]+)/g, '@/components/domain/$1');
    content = content.replace(/@\/components\/CommonHeader\.vue/g, '@/components/common/CommonHeader.vue');
    content = content.replace(/@\/components\/MarkdownRenderer\.vue/g, '@/components/common/MarkdownRenderer.vue');
    content = content.replace(/@\/components\/CreativeAside\.vue/g, '@/components/domain/CreativeAside.vue');
    content = content.replace(/@\/components\/CreativeHeader\.vue/g, '@/components/domain/CreativeHeader.vue');
    content = content.replace(/@\/components\/TiptapToolbar\.vue/g, '@/components/domain/TiptapToolbar.vue');

    if (content !== originalContent) {
        fs.writeFileSync(file, content, 'utf8');
        console.log(`Updated ${file}`);
        modifiedCount++;
    }
});

console.log(`Finished processing. Modified ${modifiedCount} files.`);
