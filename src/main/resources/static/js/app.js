(() => {

    /*
     * ==========================================================
     * DRAWER
     * ==========================================================
     */

    const drawer =
        document.querySelector('[data-drawer]');

    document
        .querySelectorAll('[data-menu-toggle]')
        .forEach(button => {

            button.addEventListener(
                'click',
                () => {

                    drawer?.classList.add('open');

                    document.body.classList.add(
                        'drawer-open'
                    );
                }
            );
        });


    document
        .querySelectorAll('[data-menu-close]')
        .forEach(button => {

            button.addEventListener(
                'click',
                () => {

                    drawer?.classList.remove('open');

                    document.body.classList.remove(
                        'drawer-open'
                    );
                }
            );
        });


    document.addEventListener(
        'keydown',
        event => {

            if (event.key === 'Escape') {

                drawer?.classList.remove('open');

                document.body.classList.remove(
                    'drawer-open'
                );
            }
        }
    );


    /*
     * ==========================================================
     * PASSWORD VISIBILITY
     * ==========================================================
     */

    document
        .querySelectorAll('[data-toggle-password]')
        .forEach(button => {

            button.addEventListener(
                'click',
                () => {

                    const input =
                        document.getElementById(
                            button.dataset.togglePassword
                        );

                    if (!input) {
                        return;
                    }

                    const showing =
                        input.type === 'text';

                    input.type =
                        showing
                            ? 'password'
                            : 'text';

                    button.textContent =
                        showing
                            ? '👁'
                            : '🙈';
                }
            );
        });


    /*
     * ==========================================================
     * RECEIPT NUMBER FORMAT
     * ==========================================================
     */

    document
        .querySelectorAll('.receipt-input')
        .forEach(input => {

            input.addEventListener(
                'input',
                () => {

                    input.value =
                        input.value
                            .replace(
                                /[^a-z0-9]/gi,
                                ''
                            )
                            .toUpperCase()
                            .slice(
                                0,
                                13
                            );
                }
            );
        });


    /*
     * ==========================================================
     * FLASH MESSAGE ANIMATION
     * ==========================================================
     */

    document
        .querySelectorAll('.flash')
        .forEach(element => {

            element.classList.add(
                'flash-visible'
            );

            setTimeout(
                () => {

                    element.classList.add(
                        'flash-leaving'
                    );

                    setTimeout(
                        () => element.remove(),
                        350
                    );

                },
                4500
            );
        });


    /*
     * ==========================================================
     * BUTTON / FORM LOADING STATES
     * ==========================================================
     */

    const actionOverlay =
        document.querySelector(
            '[data-action-overlay]'
        );

    const actionOverlayText =
        document.querySelector(
            '[data-action-overlay-text]'
        );


    document
        .querySelectorAll('[data-loading-form]')
        .forEach(form => {

            form.addEventListener(
                'submit',
                event => {

                    /*
                     * If browser validation fails,
                     * do not show loading UI.
                     */
                    if (!form.checkValidity()) {
                        return;
                    }

                    const submitButton =
                        form.querySelector(
                            'button[type="submit"]'
                        );

                    if (!submitButton) {
                        return;
                    }

                    /*
                     * Avoid double submit.
                     */
                    if (
                        submitButton.dataset.submitting
                        === 'true'
                    ) {

                        event.preventDefault();

                        return;
                    }

                    submitButton.dataset.submitting =
                        'true';

                    submitButton.disabled =
                        true;

                    submitButton.classList.add(
                        'button-loading'
                    );


                    const label =
                        submitButton.querySelector(
                            '.button-label'
                        );


                    const loadingText =
                        submitButton.dataset.loadingText
                        || 'Please wait...';


                    if (label) {

                        label.dataset.originalText =
                            label.textContent;

                        label.textContent =
                            loadingText;

                    } else {

                        submitButton.dataset.originalText =
                            submitButton.textContent;

                        submitButton.textContent =
                            loadingText;
                    }


                    /*
                     * Full-screen loading overlay only for
                     * actions likely to contact USCIS or update DB.
                     */
                    if (actionOverlay) {

                        actionOverlay.classList.add(
                            'visible'
                        );

                        actionOverlay.setAttribute(
                            'aria-hidden',
                            'false'
                        );

                        if (actionOverlayText) {

                            actionOverlayText.textContent =
                                loadingText;
                        }
                    }
                }
            );
        });


    /*
     * ==========================================================
     * SWITCH INTERACTION
     * ==========================================================
     */

    document
        .querySelectorAll('.switch input')
        .forEach(input => {

            input.addEventListener(
                'change',
                () => {

                    const option =
                        input.closest(
                            '.notification-option'
                        );

                    option?.classList.add(
                        'option-changed'
                    );

                    setTimeout(
                        () => {

                            option?.classList.remove(
                                'option-changed'
                            );

                        },
                        350
                    );
                }
            );
        });


    /*
     * ==========================================================
     * PAGE REVEAL
     * ==========================================================
     */

    const revealCards =
        document.querySelectorAll(
            '.reveal-card'
        );


    if ('IntersectionObserver' in window) {

        const observer =
            new IntersectionObserver(

                entries => {

                    entries.forEach(
                        entry => {

                            if (
                                entry.isIntersecting
                            ) {

                                entry.target.classList.add(
                                    'revealed'
                                );

                                observer.unobserve(
                                    entry.target
                                );
                            }
                        }
                    );
                },

                {
                    threshold: 0.08
                }
            );


        revealCards.forEach(
            card => observer.observe(card)
        );

    } else {

        revealCards.forEach(
            card => card.classList.add(
                'revealed'
            )
        );
    }


    /*
     * ==========================================================
     * PAGE LOADING BAR
     * ==========================================================
     */

    const loader =
        document.querySelector(
            '[data-page-loader]'
        );


    window.addEventListener(
        'load',
        () => {

            loader?.classList.add(
                'complete'
            );

            setTimeout(
                () => loader?.remove(),
                450
            );
        }
    );


    /*
     * ==========================================================
     * MOBILE TOUCH FEEDBACK
     * ==========================================================
     */

    document
        .querySelectorAll(
            '.mobile-touch, .btn, .case-card'
        )
        .forEach(element => {

            element.addEventListener(
                'touchstart',
                () => {

                    element.classList.add(
                        'touch-active'
                    );
                },
                {
                    passive: true
                }
            );


            element.addEventListener(
                'touchend',
                () => {

                    setTimeout(
                        () => {

                            element.classList.remove(
                                'touch-active'
                            );

                        },
                        120
                    );
                },
                {
                    passive: true
                }
            );
        });

})();