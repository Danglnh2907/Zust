document.addEventListener('DOMContentLoaded', () => {
    // Tab Switching Logic (only tabs with data-tab will be handled here)
    const tabs = document.querySelectorAll('.group-tabs a[data-tab]');
    const sections = document.querySelectorAll('.post-card');

    // tabs.forEach(tab => {
    //     tab.addEventListener('click', (e) => {
    //         e.preventDefault();
    //
    //         // Update active tab
    //         tabs.forEach(t => t.classList.remove('active'));
    //         tab.classList.add('active');
    //
    //         // Show/hide section based on data-tab value
    //         const tabId = tab.dataset.tab;
    //         sections.forEach(section => {
    //             section.style.display = (section.id === tabId) ? 'block' : 'none';
    //         });
    //     });
    // });

    tabs.forEach(tab => {
        tab.addEventListener('click', (e) => {
            const href = tab.getAttribute('href');

            // Nếu là tab nội bộ (SPA) như href="#"
            if (href === '#') {
                e.preventDefault(); // chỉ chặn nếu là "#"

                tabs.forEach(t => t.classList.remove('active'));
                tab.classList.add('active');

                const tabId = tab.textContent.trim().toLowerCase().replace(/\s+/g, '-');
                sections.forEach(section => {
                    section.style.display = (section.id === tabId) ? 'block' : 'none';
                });
            }

            // Nếu href không phải "#", thì KHÔNG chặn → để chuyển trang bình thường
        });
    });


    // Activate default tab
    const defaultTab = document.querySelector('.group-tabs a[data-tab].active');
    if (defaultTab) {
        defaultTab.click();
    }

    // Options Menu Logic
    const optionsBtns = document.querySelectorAll('.options-btn');
    const optionsMenus = document.querySelectorAll('.options-menu');

    optionsBtns.forEach((btn, index) => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            optionsMenus[index].classList.toggle('show');
        });
    });

    document.addEventListener('click', () => {
        optionsMenus.forEach(menu => menu.classList.remove('show'));
    });

    // Image Carousel Logic
    const tracks = document.querySelectorAll('.carousel-track');

    tracks.forEach(track => {
        const slides = Array.from(track.children);
        const nextButton = track.parentElement.querySelector('.carousel-btn.next');
        const prevButton = track.parentElement.querySelector('.carousel-btn.prev');

        if (slides.length > 1) {
            const slideWidth = slides[0].getBoundingClientRect().width;
            let currentIndex = 0;

            slides.forEach((slide, index) => {
                slide.style.left = slideWidth * index + 'px';
            });

            const moveToSlide = (targetIndex) => {
                track.style.transform = 'translateX(-' + (slideWidth * targetIndex) + 'px)';
                currentIndex = targetIndex;
                updateButtons();
            };

            const updateButtons = () => {
                prevButton.classList.toggle('hidden', currentIndex === 0);
                nextButton.classList.toggle('hidden', currentIndex === slides.length - 1);
            };

            nextButton.addEventListener('click', () => {
                if (currentIndex < slides.length - 1) moveToSlide(currentIndex + 1);
            });

            prevButton.addEventListener('click', () => {
                if (currentIndex > 0) moveToSlide(currentIndex - 1);
            });

            updateButtons();
        } else {
            nextButton.classList.add('hidden');
            prevButton.classList.add('hidden');
        }
    });
    document.addEventListener('DOMContentLoaded', function() {
        const menuButton = document.querySelector('.more-options-btn');
        const optionsMenu = document.querySelector('.options-menu-tab');

        menuButton.addEventListener('click', function(e) {
            e.stopPropagation();
            optionsMenu.classList.toggle('show');
        });

        optionsMenu.addEventListener('click', function(e) {
            e.stopPropagation();
        });

        document.addEventListener('click', function() {
            optionsMenu.classList.remove('show');
        });
    });


});
