<template></template>

<script setup>
const source = new EventSource("/api/test/sse");

source.onmessage = function (event) {
    console.log(event.data);
};

source.onerror = function () {
    console.error("Connection error");
    source.close();
};

const progress = [
    {
        id:1,
        type:2,
        title: "",
        content: "",
        data: {
            date:0
        }
    }
];
</script>

<style></style>