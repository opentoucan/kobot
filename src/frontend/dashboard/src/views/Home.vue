<template>
    <div class="container w-50">
        <div class="card">
        <div class="card-header">
            Status
        </div>
        <div class="card-body">
            <button class="btn btn-info" @click="onRestartDiscord">Restart Discord</button>
        </div>
        </div>
    </div>
</template>

<script>
    export default {
        name: 'Home',
        methods: {
            onRestartDiscord() {
                console.log("Click!")
            }
        }
    }
</script>
