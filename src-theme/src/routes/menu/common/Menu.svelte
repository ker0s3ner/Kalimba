<script lang="ts">
    import Header from "./header/Header.svelte";
    import {fly} from "svelte/transition";
    import {onMount} from "svelte";
    import {location} from "svelte-spa-router";
    import BeachScene from "../title/BeachScene.svelte";

    const transitionDuration = 700; // TODO: suboptimal

    let ready = false;

    onMount(() => {
        setTimeout(() => {
            ready = true;
        }, transitionDuration);
    });
</script>

<div class="menu">
    {#if $location === "/title"}
        <BeachScene/>
    {/if}

    {#if ready}
        <div transition:fly|global={{duration: 700, y: -100}}>
            <Header/>
        </div>
    {/if}

    <div class="menu-wrapper">
        <slot/>
    </div>
</div>

<style lang="scss">
  .menu {
    padding: 50px;
    display: flex;
    flex-direction: column;
    // Not sized in vh, which not every browser scales with zoom
    position: fixed;
    inset: 0;
  }

  .menu-wrapper {
    position: relative;
    z-index: 1;
    flex: 1;
    display: flex;
    flex-direction: column;
    will-change: transform;
  }

  @media screen and (max-width: 1366px) {
    .menu {
      zoom: 0.8;
    }
  }

  @media screen and (max-width: 1200px) {
    .menu {
      zoom: 0.5;
    }
  }

  @media screen and (max-height: 1100px) {
    .menu {
      zoom: 0.8;
    }
  }

  @media screen and (max-height: 700px) {
    .menu {
      zoom: 0.5;
    }
  }

  @media screen and (max-height: 540px) {
    .menu {
      zoom: 0.4;
    }
  }
</style>
