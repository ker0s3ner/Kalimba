<script lang="ts">
    import {onDestroy, onMount} from "svelte";
    import KalimbaMark from "../../../components/KalimbaMark.svelte";

    let fps = 0;
    let frames = 0;
    let last = performance.now();
    let raf = 0;

    function tick(now: number) {
        frames++;
        if (now - last >= 1000) {
            fps = frames;
            frames = 0;
            last = now;
        }
        raf = requestAnimationFrame(tick);
    }

    onMount(() => {
        raf = requestAnimationFrame(tick);
    });

    onDestroy(() => cancelAnimationFrame(raf));
</script>

<div class="pill">
    <KalimbaMark size={18}/>
    <span class="name">Kalimba</span>
    <span class="dot">&bull;</span>
    <span class="val">{fps}<span class="unit">fps</span></span>
</div>

<style lang="scss">
  .pill {
    display: inline-flex;
    align-items: center;
    gap: 7px;
    padding: 5px 12px;
    border-radius: 999px;
    font-size: 12px;
    font-weight: 600;
    color: var(--text-color);
    background: color-mix(in srgb, var(--surface-color) 78%, transparent);
    border: 1px solid color-mix(in srgb, var(--accent-color) 40%, transparent);
    box-shadow: 0 4px 14px color-mix(in srgb, black 40%, transparent);
    backdrop-filter: blur(8px);
  }

  .dot {
    color: var(--sea-color);
  }

  .unit {
    color: var(--text-dimmed-color);
    margin-left: 2px;
    font-weight: 500;
  }
</style>
