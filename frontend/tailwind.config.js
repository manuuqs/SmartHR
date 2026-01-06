/** @type {import('tailwindcss').Config} */

export default {
    content: [
        "./index.html",
        "./src/**/*.{js,jsx}",
    ],

    theme: {
        extend: {
            animation: {
                'fade-in': 'fadeIn 1s ease-in-out',
                'slide-in': 'slideIn 0.8s ease-out',
            },
            keyframes: {
                fadeIn: {
                    '0%': { opacity: 0 },
                    '100%': { opacity: 1 },
                },
                slideIn: {
                    '0%': { transform: 'translateX(50px)', opacity: 0 },
                    '100%': { transform: 'translateX(0)', opacity: 1 },
                },
            },
        },
    },
    plugins: [],
};


