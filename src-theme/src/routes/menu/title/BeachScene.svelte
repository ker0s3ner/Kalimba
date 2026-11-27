<!-- Full-bleed beach backdrop for the title screen: sky, sun, animated sea, sand and palms. -->
<div class="beach" aria-hidden="true">
    <svg class="scene" viewBox="0 0 1600 900" preserveAspectRatio="xMidYMax slice">
        <defs>
            <linearGradient id="sky" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0" stop-color="#5fb7d8"/>
                <stop offset=".55" stop-color="#a9dcea"/>
                <stop offset="1" stop-color="#fbe7c0"/>
            </linearGradient>
            <radialGradient id="sun" cx=".5" cy=".5" r=".5">
                <stop offset="0" stop-color="#fff6cf" stop-opacity="1"/>
                <stop offset=".35" stop-color="#ffe49a" stop-opacity=".75"/>
                <stop offset="1" stop-color="#ffd27a" stop-opacity="0"/>
            </radialGradient>
            <linearGradient id="sea" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0" stop-color="#2f9fb8"/>
                <stop offset="1" stop-color="#5cc7c4"/>
            </linearGradient>
            <linearGradient id="sand" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0" stop-color="#f3dca4"/>
                <stop offset="1" stop-color="#d9b676"/>
            </linearGradient>
        </defs>

        <rect width="1600" height="900" fill="url(#sky)"/>
        <circle cx="330" cy="190" r="260" fill="url(#sun)"/>
        <circle cx="330" cy="190" r="62" fill="#fff4c2"/>

        <g fill="#ffffff" opacity=".85" class="clouds">
            <g class="cloud c1">
                <ellipse cx="640" cy="150" rx="90" ry="22"/>
                <ellipse cx="700" cy="136" rx="60" ry="24"/>
                <ellipse cx="590" cy="142" rx="50" ry="18"/>
            </g>
            <g class="cloud c2">
                <ellipse cx="1180" cy="230" rx="110" ry="24"/>
                <ellipse cx="1240" cy="214" rx="70" ry="26"/>
            </g>
        </g>

        <!-- sea -->
        <rect y="470" width="1600" height="230" fill="url(#sea)"/>
        <rect y="470" width="1600" height="3" fill="#ffffff" opacity=".6"/>
        <g class="waves w1" fill="none" stroke="#ffffff" stroke-opacity=".55" stroke-width="3" stroke-linecap="round">
            {#each Array(12) as _, i}
                <path d={`M${i * 150 - 60} 520 q 30 -14 60 0 t 60 0`}/>
            {/each}
        </g>
        <g class="waves w2" fill="none" stroke="#ffffff" stroke-opacity=".4" stroke-width="3" stroke-linecap="round">
            {#each Array(12) as _, i}
                <path d={`M${i * 150 + 20} 580 q 30 -14 60 0 t 60 0`}/>
            {/each}
        </g>
        <!-- foam line -->
        <path d="M0 690 q 100 -22 200 0 t 200 0 t 200 0 t 200 0 t 200 0 t 200 0 t 200 0 t 200 0 V 720 H 0 Z"
              fill="#ffffff" opacity=".75" class="foam"/>

        <!-- sand -->
        <path d="M0 700 q 200 -28 400 0 t 400 0 t 400 0 t 400 0 V 900 H 0 Z" fill="url(#sand)"/>
        <g fill="#c9a263" opacity=".55">
            {#each [[180, 790], [420, 840], [720, 810], [980, 860], [1280, 800], [1450, 845], [90, 860]] as [x, y]}
                <ellipse cx={x} cy={y} rx="14" ry="3.5"/>
            {/each}
        </g>

        <!-- palm trees -->
        <g class="palm" transform="translate(1380 700)">
            <path d="M0 0 C 14 -120 -6 -230 22 -340" stroke="#7a4f27" stroke-width="22" fill="none"
                  stroke-linecap="round"/>
            <g fill="#2f8a4d" transform="translate(22 -340)">
                <path d="M0 0 C 60 -50 140 -40 190 10 C 120 -10 60 -10 0 0Z"/>
                <path d="M0 0 C -60 -50 -140 -40 -190 10 C -120 -10 -60 -10 0 0Z"/>
                <path d="M0 0 C 30 -80 100 -110 150 -90 C 90 -80 50 -50 0 0Z"/>
                <path d="M0 0 C -30 -80 -100 -110 -150 -90 C -90 -80 -50 -50 0 0Z"/>
                <path d="M0 0 C 40 30 110 60 160 120 C 100 80 40 60 0 0Z" fill="#26763f"/>
                <path d="M0 0 C -40 30 -110 60 -160 120 C -100 80 -40 60 0 0Z" fill="#26763f"/>
                <circle cx="-10" cy="12" r="12" fill="#6b4a25"/>
                <circle cx="10" cy="14" r="12" fill="#6b4a25"/>
            </g>
        </g>
        <g class="palm" transform="translate(120 720) scale(.8)">
            <path d="M0 0 C -18 -110 8 -220 -16 -310" stroke="#7a4f27" stroke-width="20" fill="none"
                  stroke-linecap="round"/>
            <g fill="#2f8a4d" transform="translate(-16 -310)">
                <path d="M0 0 C 60 -50 140 -40 190 10 C 120 -10 60 -10 0 0Z"/>
                <path d="M0 0 C -60 -50 -140 -40 -190 10 C -120 -10 -60 -10 0 0Z"/>
                <path d="M0 0 C 30 -80 100 -110 150 -90 C 90 -80 50 -50 0 0Z"/>
                <path d="M0 0 C -30 -80 -100 -110 -150 -90 C -90 -80 -50 -50 0 0Z"/>
                <circle cx="-8" cy="12" r="11" fill="#6b4a25"/>
            </g>
        </g>
    </svg>
</div>

<style>
    .beach {
        position: absolute;
        inset: 0;
        z-index: 0;
        overflow: hidden;
        background: #a9dcea;
    }

    .scene {
        width: 100%;
        height: 100%;
        display: block;
    }

    .waves.w1 {
        animation: drift 9s ease-in-out infinite alternate;
    }

    .waves.w2 {
        animation: drift 12s ease-in-out infinite alternate-reverse;
    }

    .foam {
        animation: lap 6s ease-in-out infinite;
    }

    .cloud.c1 {
        animation: cloud 60s linear infinite alternate;
    }

    .cloud.c2 {
        animation: cloud 80s linear infinite alternate-reverse;
    }

    .palm {
        transform-box: fill-box;
        transform-origin: bottom center;
    }

    @keyframes drift {
        from {
            transform: translateX(-30px);
        }
        to {
            transform: translateX(30px);
        }
    }

    @keyframes lap {
        0%, 100% {
            transform: translateY(0);
            opacity: .75;
        }
        50% {
            transform: translateY(-10px);
            opacity: .5;
        }
    }

    @keyframes cloud {
        from {
            transform: translateX(-60px);
        }
        to {
            transform: translateX(120px);
        }
    }
</style>
