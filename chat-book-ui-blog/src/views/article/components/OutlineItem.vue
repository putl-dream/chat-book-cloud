<!-- src/components/OutlineItem.vue -->
<template>
    <li>
        <div class="article-outline-item">
            <button @click="toggleCollapse" class="article-outline-collapse-button">
                {{ item.collapsed ? '📥' : '📤' }}
            </button>
            {{ item.title }}
        </div>
        <ul v-show="!item.collapsed" style="padding-left: 40px">
            <OutlineItem v-for="(subItem, subIndex) in item.subItems" :key="subIndex" :item="subItem" />
        </ul>
    </li>
</template>

<script setup>
import {ref} from 'vue';

const props = defineProps({
    item: {
        type: Object,
        required: true,
    },
});

const collapsed = ref(props.item.collapsed || false);

const toggleCollapse = () => {
    collapsed.value = !collapsed.value;
    emit('toggle-collapse', {index: props.item.index, collapsed: collapsed.value});
};

const emit = defineEmits(['toggle-collapse']);
</script>
