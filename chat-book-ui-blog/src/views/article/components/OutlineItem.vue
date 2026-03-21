<!-- src/components/OutlineItem.vue -->
<template>
    <li>
        <div class="outline-item">
            <button @click="toggleCollapse" class="collapse-button">
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
import {ref, computed} from 'vue';

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

<style scoped>
.outline-item {
    display: flex;
    align-items: center;
}

.collapse-button {
    padding: 0 5px;
    border: none;
    background: none;
    color: #007bff;
    cursor: pointer;
    font-size: 1.2em;
    margin-right: 5px;
}


</style>
