<script lang="ts">
    import {onDestroy, onMount} from "svelte";
    import {REST_BASE} from "../../../integration/host";

    // One skin pixel is drawn as U canvas pixels so the character stays crisp.
    const U = 9;
    const W = 420;
    const H = 360;

    let canvas: HTMLCanvasElement;
    let raf = 0;
    let skin: HTMLImageElement | null = null;
    let slim = false;
    let notes: { id: number; x: number; glyph: string }[] = [];
    let noteTimer: number | undefined;
    let noteId = 0;

    onMount(async () => {
        try {
            const response = await fetch(`${REST_BASE}/api/v1/client/resource/ownSkin`);
            if (response.ok) {
                slim = response.headers.get("X-Skin-Slim") === "true";
                const img = new Image();
                img.src = URL.createObjectURL(await response.blob());
                await img.decode();
                skin = img;
            }
        } catch {
            skin = null;
        }

        raf = requestAnimationFrame(draw);
        noteTimer = window.setInterval(() => {
            const id = noteId++;
            notes = [...notes.slice(-4), {id, x: 150 + Math.random() * 110, glyph: ["♪", "♫", "♩"][id % 3]}];
        }, 1100);
    });

    onDestroy(() => {
        cancelAnimationFrame(raf);
        clearInterval(noteTimer);
    });

    /** Draws a skin region (skin pixel units) with its top-left at 0,0, optionally squashed to a height. */
    function region(ctx: CanvasRenderingContext2D, sx: number, sy: number, sw: number, sh: number,
                    ox = 0, oy = 0, height = sh * U) {
        if (skin) {
            ctx.drawImage(skin, sx, sy, sw, sh, ox, oy, sw * U, height);
        } else {
            ctx.fillStyle = "#c98a45";
            ctx.fillRect(ox, oy, sw * U, height);
        }
    }

    /** A body part with its second skin layer, anchored so (0,0) is the top-centre of the part. */
    function layered(ctx: CanvasRenderingContext2D, base: [number, number], overlay: [number, number],
                     sw: number, sh: number, rowFrom = 0, rowTo = sh, height = (rowTo - rowFrom) * U) {
        const ox = -sw * U / 2;
        const rows = rowTo - rowFrom;
        region(ctx, base[0], base[1] + rowFrom, sw, rows, ox, 0, height);
        region(ctx, overlay[0], overlay[1] + rowFrom, sw, rows, ox - U * 0.1, -U * 0.05, height + U * 0.1);
    }

    function drawLeg(ctx: CanvasRenderingContext2D, x: number, y: number, towardLeft: boolean,
                     base: [number, number], overlay: [number, number]) {
        ctx.save();
        ctx.translate(x, y);
        ctx.rotate((towardLeft ? 1 : -1) * (Math.PI / 2 + 0.06));
        layered(ctx, base, overlay, 4, 12);
        ctx.restore();
    }

    function drawKalimba(ctx: CanvasRenderingContext2D, t: number, flick: number) {
        const w = 9 * U;
        const h = 7 * U;
        ctx.save();
        ctx.translate(-w / 2, 0);

        // soft shadow on the lap
        ctx.fillStyle = "rgba(0,0,0,.25)";
        ctx.beginPath();
        ctx.roundRect(4, 7, w, h, 10);
        ctx.fill();

        // wooden body with a vertical sheen
        const wood = ctx.createLinearGradient(0, 0, w, 0);
        wood.addColorStop(0, "#9a6430");
        wood.addColorStop(0.5, "#c58a4b");
        wood.addColorStop(1, "#8a5527");
        ctx.fillStyle = wood;
        ctx.beginPath();
        ctx.roundRect(0, 0, w, h, 10);
        ctx.fill();
        ctx.strokeStyle = "#5e3a18";
        ctx.lineWidth = 2;
        ctx.stroke();

        // grain
        ctx.strokeStyle = "rgba(70,40,15,.35)";
        ctx.lineWidth = 1;
        for (let i = 0; i < 6; i++) {
            ctx.beginPath();
            ctx.moveTo(6, 14 + i * 9);
            ctx.bezierCurveTo(w * 0.3, 10 + i * 9, w * 0.7, 20 + i * 9, w - 6, 13 + i * 9);
            ctx.stroke();
        }

        // sound hole
        ctx.fillStyle = "#2a190c";
        ctx.beginPath();
        ctx.ellipse(w / 2, h - 18, 9, 9, 0, 0, Math.PI * 2);
        ctx.fill();
        ctx.strokeStyle = "#e0b070";
        ctx.lineWidth = 2;
        ctx.stroke();

        // bridge bar
        ctx.fillStyle = "#d9d9d9";
        ctx.fillRect(4, 22, w - 8, 4);
        ctx.fillStyle = "#8c8c8c";
        ctx.fillRect(4, 26, w - 8, 2);

        // tines: longest in the middle, silver with a bright tip
        const n = 9;
        for (let i = 0; i < n; i++) {
            const x = 8 + i * ((w - 16) / (n - 1));
            const len = 36 - Math.abs(4 - i) * 4.2;
            const bend = i === flick ? 2.5 : 0;
            ctx.fillStyle = "#c9ccd1";
            ctx.fillRect(x - 1.7, 24 - len + bend, 3.4, len);
            ctx.fillStyle = "#f4f6f8";
            ctx.fillRect(x - 1.7, 24 - len + bend, 3.4, 5);
        }
        ctx.restore();
    }

    function drawArm(ctx: CanvasRenderingContext2D, side: -1 | 1, shoulder: { x: number; y: number },
                     hand: { x: number; y: number }, aw: number, t: number) {
        // side -1 is the viewer's left, which is the character's right arm
        const base: [number, number] = side === -1 ? [40, 16] : [32, 48];
        const overlay: [number, number] = side === -1 ? [40, 32] : [48, 48];
        const front = (b: [number, number]): [number, number] => [b[0] + 4, b[1] + 4];

        const upperLen = 6 * U;
        const elbow = {x: shoulder.x + side * 1.6 * U, y: shoulder.y + upperLen * 0.86};

        // upper arm, tilted so the elbow sits slightly out
        ctx.save();
        ctx.translate(shoulder.x, shoulder.y);
        ctx.rotate(-side * 0.27);
        layered(ctx, front(base), front(overlay), aw, 12, 0, 6);
        ctx.restore();

        // forearm, stretched from the elbow to the hand on the kalimba
        const dx = hand.x - elbow.x;
        const dy = hand.y - elbow.y;
        const len = Math.hypot(dx, dy);
        ctx.save();
        ctx.translate(elbow.x, elbow.y);
        ctx.rotate(Math.atan2(-dx, dy));
        layered(ctx, front(base), front(overlay), aw, 12, 6, 12, len);
        ctx.restore();

        // thumb resting on the tines
        const tap = Math.max(0, Math.sin(t * 6 + (side === -1 ? 0 : 1.6))) * 3;
        ctx.fillStyle = skin ? sampleSkin(front(base)) : "#e0b48a";
        ctx.beginPath();
        ctx.roundRect(hand.x - side * 8 - 5, hand.y - 12 + tap, 11, 10, 3);
        ctx.fill();
    }

    let skinTone = "#e0b48a";
    let toneCtx: CanvasRenderingContext2D | null = null;

    function sampleSkin(front: [number, number]) {
        if (!skin) return skinTone;
        if (!toneCtx) {
            const c = document.createElement("canvas");
            c.width = c.height = 64;
            toneCtx = c.getContext("2d", {willReadFrequently: true});
            toneCtx?.drawImage(skin, 0, 0);
        }
        const p = toneCtx?.getImageData(front[0] + 1, front[1] + 11, 1, 1).data;
        if (p) skinTone = `rgb(${p[0]},${p[1]},${p[2]})`;
        return skinTone;
    }

    function draw(now: number) {
        const ctx = canvas?.getContext("2d");
        if (!ctx) {
            raf = requestAnimationFrame(draw);
            return;
        }

        const t = now / 1000;
        const sway = Math.sin(t * 1.5) * 1.5;
        const aw = slim ? 3 : 4;
        ctx.clearRect(0, 0, W, H);
        ctx.imageSmoothingEnabled = false;

        const cx = W / 2;
        const hipY = 258;

        // legs lie crossed in front, drawn first so the torso covers the hips
        drawLeg(ctx, cx - 1.5 * U, hipY + 2 * U, true, [4, 20], [4, 36]);
        drawLeg(ctx, cx + 1.5 * U, hipY + 2 * U + 4, false, [20, 52], [4, 52]);

        // torso
        ctx.save();
        ctx.translate(cx, hipY - 12 * U + sway * 0.4);
        layered(ctx, [20, 20], [20, 36], 8, 12);
        ctx.restore();

        // head, with a lazy tilt in time with the music
        ctx.save();
        ctx.translate(cx, hipY - 12 * U + sway * 0.4);
        ctx.rotate(Math.sin(t * 1.1) * 0.05);
        ctx.translate(0, -8 * U);
        layered(ctx, [8, 8], [40, 8], 8, 8);
        ctx.restore();

        // the kalimba is held against the belly
        const kx = cx;
        const ky = hipY - 8.2 * U + sway * 0.4;
        ctx.save();
        ctx.translate(kx, ky);
        ctx.rotate(-0.04);
        drawKalimba(ctx, t, Math.floor(t * 4) % 9);
        ctx.restore();

        // arms reach in from the shoulders to the sides of the kalimba
        const shoulderY = hipY - 11.5 * U + sway * 0.4;
        const handY = ky + 2.4 * U;
        drawArm(ctx, -1, {x: cx - (4 + aw / 2) * U, y: shoulderY}, {x: kx - 4.3 * U, y: handY}, aw, t);
        drawArm(ctx, 1, {x: cx + (4 + aw / 2) * U, y: shoulderY}, {x: kx + 4.3 * U, y: handY}, aw, t);

        raf = requestAnimationFrame(draw);
    }
</script>

<div class="spot" aria-hidden="true">
    <!-- beach umbrella: pole, then the canopy tilted over the player -->
    <svg class="umbrella" viewBox="0 0 520 520">
        <ellipse cx="260" cy="488" rx="190" ry="20" fill="rgba(80,50,10,.22)"/>
        <!-- towel -->
        <g>
            <path d="M70 452 L450 452 L480 492 L40 492 Z" fill="#f3e6d0"/>
            <path d="M80 460 L440 460 L452 474 L68 474 Z" fill="#4fb3a9"/>
            <path d="M52 480 L468 480 L474 488 L46 488 Z" fill="#d9923b"/>
        </g>
        <line x1="352" y1="150" x2="420" y2="500" stroke="#7a4f27" stroke-width="9" stroke-linecap="round"/>
        <g transform="rotate(-7 260 100)">
            <path d="M40 130 Q 260 -60 480 130 Z" fill="#f3e6d0"/>
            <path d="M40 130 Q 100 20 205 -2 Q 190 70 175 130 Z" fill="#d9923b" opacity=".95"/>
            <path d="M260 -18 Q 260 60 262 130 L 175 130 Q 190 70 205 -2 Z" fill="#4fb3a9" opacity=".0"/>
            <path d="M205 -2 Q 235 -8 262 -12 Q 270 60 262 130 L 175 130 Q 190 70 205 -2 Z" fill="#4fb3a9"/>
            <path d="M262 -12 Q 340 4 395 40 Q 345 80 350 130 L 262 130 Q 270 60 262 -12 Z" fill="#d9923b"/>
            <path d="M395 40 Q 450 75 480 130 L 350 130 Q 345 80 395 40 Z" fill="#4fb3a9"/>
            <path d="M40 130 Q 60 150 85 130 Q 110 150 135 130 Q 160 150 185 130 Q 210 150 235 130 Q 260 150 285 130
                     Q 310 150 335 130 Q 360 150 385 130 Q 410 150 435 130 Q 460 150 480 130 Z"
                  fill="#e8d6b4"/>
            <circle cx="262" cy="-14" r="6" fill="#7a4f27"/>
        </g>
    </svg>

    <canvas bind:this={canvas} width={W} height={H}></canvas>

    {#each notes as note (note.id)}
        <span class="note" style="left: {note.x + 50}px">{note.glyph}</span>
    {/each}
</div>

<style lang="scss">
  .spot {
    position: relative;
    width: 520px;
    height: 520px;
    pointer-events: none;
    /* scale the whole vignette with the window height so it never overflows */
    transform: scale(min(calc(100vh / 800), calc(100vw / 1300)));
    transform-origin: bottom right;
  }

  .umbrella {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
  }

  canvas {
    position: absolute;
    left: 50px;
    top: 150px;
    width: 420px;
    height: 360px;
    image-rendering: pixelated;
    filter: drop-shadow(0 6px 8px rgba(80, 50, 10, .3));
  }

  .note {
    position: absolute;
    top: 250px;
    font-size: 26px;
    color: #1f7d8c;
    text-shadow: 0 1px 0 #fff;
    animation: float 2.6s ease-out forwards;
  }

  @keyframes float {
    from {
      opacity: 0;
      transform: translateY(30px);
    }
    20% {
      opacity: 1;
    }
    to {
      opacity: 0;
      transform: translateY(-90px) rotate(14deg);
    }
  }
</style>
