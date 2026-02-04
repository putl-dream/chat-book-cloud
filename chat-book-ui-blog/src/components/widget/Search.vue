<template>
    <div class="search-bar">
        <input type="text" placeholder="搜索..."/>
        <button>搜索</button>
    </div>
</template>

<style scoped>

.search-bar {
    display: flex;
    border: 1px solid #eaeaea;
    border-radius: 20px;
    overflow: hidden;
    max-height: 30px;
    justify-content: center;
    min-width: 450px;
}

.search-bar input {
    border: none;
    padding: 8px 12px;
    width: 100%;
    outline: none;
}

.search-bar button {
    width: 90px;
    background-color: #6754f8;
    color: white;
    border: none;
    cursor: pointer;
    border-radius: 0 20px 20px 0;
}
</style>