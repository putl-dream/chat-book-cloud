import { VueRenderer } from '@tiptap/vue-3'
import tippy from 'tippy.js'
import CommandList from './CommandList.vue'
import { icons } from '../icons.js'

export default {
  items: ({ query }) => {
    return [
      {
        title: '一级标题',
        searchTerms: ['yijibiaoti', 'yjbt', 'h1', 'heading', 'yiji'],
        icon: icons.h1,
        command: ({ editor, range }) => {
          editor
            .chain()
            .focus()
            .deleteRange(range)
            .setNode('heading', { level: 1 })
            .run()
        },
      },
      {
        title: '二级标题',
        searchTerms: ['erjibiaoti', 'ejbt', 'h2', 'heading', 'erji'],
        icon: icons.h2,
        command: ({ editor, range }) => {
          editor
            .chain()
            .focus()
            .deleteRange(range)
            .setNode('heading', { level: 2 })
            .run()
        },
      },
      {
        title: '三级标题',
        searchTerms: ['sanjibiaoti', 'sjbt', 'h3', 'heading', 'sanji'],
        icon: icons.h3,
        command: ({ editor, range }) => {
          editor
            .chain()
            .focus()
            .deleteRange(range)
            .setNode('heading', { level: 3 })
            .run()
        },
      },
      {
        title: '无序列表',
        searchTerms: ['wuxuliebiao', 'wxlb', 'bullet', 'list', 'wuxu'],
        icon: icons.bulletList,
        command: ({ editor, range }) => {
          editor
            .chain()
            .focus()
            .deleteRange(range)
            .toggleBulletList()
            .run()
        },
      },
      {
        title: '有序列表',
        searchTerms: ['youxuliebiao', 'yxlb', 'ordered', 'list', 'youxu'],
        icon: icons.orderedList,
        command: ({ editor, range }) => {
          editor
            .chain()
            .focus()
            .deleteRange(range)
            .toggleOrderedList()
            .run()
        },
      },
      {
        title: '任务列表',
        searchTerms: ['renwuliebiao', 'rwlb', 'task', 'list', 'renwu', 'todo'],
        icon: icons.taskList,
        command: ({ editor, range }) => {
          editor
            .chain()
            .focus()
            .deleteRange(range)
            .toggleTaskList()
            .run()
        },
      },
      {
        title: '代码块',
        searchTerms: ['daimakuai', 'dmk', 'code', 'block', 'daima'],
        icon: icons.codeBlock,
        command: ({ editor, range }) => {
          editor
            .chain()
            .focus()
            .deleteRange(range)
            .toggleCodeBlock()
            .run()
        },
      },
      {
        title: '引用',
        searchTerms: ['yinyong', 'yy', 'blockquote', 'quote'],
        icon: icons.blockquote,
        command: ({ editor, range }) => {
          editor
            .chain()
            .focus()
            .deleteRange(range)
            .toggleBlockquote()
            .run()
        },
      },
      {
        title: '分割线',
        searchTerms: ['fengexian', 'fgx', 'horizontal', 'rule', 'line', 'fenge'],
        icon: icons.horizontalRule,
        command: ({ editor, range }) => {
          editor
            .chain()
            .focus()
            .deleteRange(range)
            .setHorizontalRule()
            .run()
        },
      },
    ].filter(item => {
      if (!query) return true;
      const lowerQuery = query.toLowerCase();
      return item.title.includes(lowerQuery) || 
             item.searchTerms.some(term => term.includes(lowerQuery));
    }).slice(0, 10)
  },

  render: () => {
    let component
    let popup

    return {
      onStart: props => {
        component = new VueRenderer(CommandList, {
          props,
          editor: props.editor,
        })

        if (!props.clientRect) {
          return
        }

        popup = tippy('body', {
          getReferenceClientRect: props.clientRect,
          appendTo: () => document.body,
          content: component.element,
          showOnCreate: true,
          interactive: true,
          trigger: 'manual',
          placement: 'bottom-start',
        })
      },

      onUpdate(props) {
        component.updateProps(props)

        if (!props.clientRect) {
          return
        }

        popup[0].setProps({
          getReferenceClientRect: props.clientRect,
        })
      },

      onKeyDown(props) {
        if (props.event.key === 'Escape') {
          popup[0].hide()

          return true
        }

        return component.ref?.onKeyDown(props)
      },

      onExit() {
        popup[0].destroy()
        component.destroy()
      },
    }
  },
}
