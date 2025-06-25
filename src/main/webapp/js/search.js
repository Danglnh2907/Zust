/**
 * Live Search functionality for Zust social media platform
 * Provides real-time search with dropdown results similar to Facebook
 */
class LiveSearch {
    constructor() {
        this.searchInput = document.getElementById('liveSearchInput');
        this.searchDropdown = document.getElementById('liveSearchDropdown');
        this.searchLoading = document.getElementById('searchLoading');
        this.searchFrame = document.getElementById('liveSearchFrame');
        this.debounceTimeout = null;
        this.currentKeyword = '';
        this.isLoading = false;
        this.contextPath = this.getContextPath();

        // Kiểm tra elements tồn tại
        if (!this.searchInput || !this.searchDropdown || !this.searchLoading || !this.searchFrame) {
            console.warn('LiveSearch: Some required elements not found');
            return;
        }

        this.init();
        console.log('LiveSearch initialized');
    }

    init() {
        // Event listener cho input search
        this.searchInput.addEventListener('input', (e) => {
            this.handleSearchInput(e.target.value);
        });

        // Show dropdown khi focus nếu có kết quả
        this.searchInput.addEventListener('focus', () => {
            if (this.currentKeyword && this.currentKeyword.length >= 2) {
                this.showDropdown();
            }
        });

        // Clear search khi blur (tùy chọn)
        this.searchInput.addEventListener('blur', () => {
            // Delay để cho phép click vào dropdown items
            setTimeout(() => {
                this.hideDropdown();
            }, 200);
        });

        // Click outside để hide dropdown
        document.addEventListener('click', (e) => {
            if (!e.target.closest('.search-container')) {
                this.hideDropdown();
            }
        });

        // Escape key để hide dropdown
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.hideDropdown();
                this.searchInput.blur();
            }

            // Enter key để search full
            if (e.key === 'Enter' && e.target === this.searchInput) {
                e.preventDefault();
                this.performFullSearch();
            }
        });

        // Arrow key navigation (optional enhancement)
        this.searchInput.addEventListener('keydown', (e) => {
            if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
                e.preventDefault();
                this.navigateResults(e.key === 'ArrowDown' ? 'down' : 'up');
            }
        });
    }

    handleSearchInput(keyword) {
        keyword = keyword.trim();
        this.currentKeyword = keyword;

        // Clear previous timeout
        if (this.debounceTimeout) {
            clearTimeout(this.debounceTimeout);
        }

        // Hide dropdown nếu keyword quá ngắn
        if (keyword.length < 2) {
            this.hideDropdown();
            this.hideLoading();
            return;
        }

        // Show loading ngay lập tức để UX tốt hơn
        this.showLoading();

        // Debounce search để tránh quá nhiều requests
        this.debounceTimeout = setTimeout(() => {
            this.performLiveSearch(keyword);
        }, 300); // 300ms debounce
    }

    performLiveSearch(keyword) {
        if (this.isLoading) {
            return; // Prevent multiple simultaneous requests
        }

        this.isLoading = true;
        const searchUrl = `${this.contextPath}/search?action=live&keyword=${encodeURIComponent(keyword)}`;

        console.log('Performing live search for:', keyword);

        // Configure iframe load event
        this.searchFrame.onload = () => {
            try {
                const frameDoc = this.searchFrame.contentDocument || this.searchFrame.contentWindow.document;

                if (!frameDoc || !frameDoc.body) {
                    throw new Error('Cannot access iframe content');
                }

                const resultsHtml = frameDoc.body.innerHTML;

                this.hideLoading();
                this.isLoading = false;

                if (resultsHtml.trim()) {
                    this.displayResults(resultsHtml);
                    this.showDropdown();
                } else {
                    this.displayNoResults();
                }

            } catch (error) {
                console.error('Error loading search results:', error);
                this.hideLoading();
                this.isLoading = false;
                this.displayError();
            }
        };

        // Handle iframe load errors
        this.searchFrame.onerror = () => {
            console.error('Error loading search iframe');
            this.hideLoading();
            this.isLoading = false;
            this.displayError();
        };

        // Load search results in iframe
        this.searchFrame.src = searchUrl;
    }

    displayResults(resultsHtml) {
        const container = this.searchDropdown.querySelector('.search-results-container');
        if (!container) {
            console.error('Search results container not found');
            return;
        }

        container.innerHTML = resultsHtml;

        // Add click handlers for "See all" links
        container.querySelectorAll('.view-all-link').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const category = link.getAttribute('data-category');
                if (category && this.currentKeyword) {
                    this.viewMore(this.currentKeyword, category);
                }
            });
        });

        // Add click handlers for search items
        container.querySelectorAll('.search-item').forEach(item => {
            item.addEventListener('click', (e) => {
                e.preventDefault();
                const itemType = item.getAttribute('data-type');
                const itemId = item.getAttribute('data-id');
                if (itemType && itemId) {
                    this.handleItemClick(itemType, itemId);
                }
            });
        });

        // Highlight search terms in results
        this.highlightSearchTerms(container, this.currentKeyword);

        console.log('Search results displayed for:', this.currentKeyword);
    }

    displayNoResults() {
        const container = this.searchDropdown.querySelector('.search-results-container');
        if (container) {
            container.innerHTML = `
                <div class="no-results">
                    <i class="fas fa-search" style="margin-right: 8px; color: #65676b;"></i>
                    No results found for "${this.escapeHtml(this.currentKeyword)}"
                </div>
            `;
            this.showDropdown();
        }
    }

    displayError() {
        const container = this.searchDropdown.querySelector('.search-results-container');
        if (container) {
            container.innerHTML = `
                <div class="no-results">
                    <i class="fas fa-exclamation-triangle" style="margin-right: 8px; color: #e74c3c;"></i>
                    Search temporarily unavailable. Please try again.
                </div>
            `;
            this.showDropdown();
        }
    }

    highlightSearchTerms(container, searchTerm) {
        if (!searchTerm || searchTerm.length < 2) return;

        const textNodes = this.getTextNodes(container);
        const regex = new RegExp(`(${this.escapeRegex(searchTerm)})`, 'gi');

        textNodes.forEach(node => {
            const parent = node.parentNode;
            if (parent && !parent.closest('.search-highlight')) {
                const highlightedText = node.textContent.replace(regex, '<span class="search-highlight">$1</span>');
                if (highlightedText !== node.textContent) {
                    const wrapper = document.createElement('span');
                    wrapper.innerHTML = highlightedText;
                    parent.replaceChild(wrapper, node);
                }
            }
        });
    }

    getTextNodes(element) {
        const textNodes = [];
        const walker = document.createTreeWalker(
            element,
            NodeFilter.SHOW_TEXT,
            null,
            false
        );

        let node;
        while (node = walker.nextNode()) {
            if (node.textContent.trim()) {
                textNodes.push(node);
            }
        }

        return textNodes;
    }

    viewMore(keyword, category) {
        const viewMoreUrl = `${this.contextPath}/search?action=viewMore&keyword=${encodeURIComponent(keyword)}&category=${encodeURIComponent(category)}`;
        console.log('Navigating to view more:', viewMoreUrl);
        window.location.href = viewMoreUrl;
    }

    handleItemClick(itemType, itemId) {
        let redirectUrl = '';

        switch (itemType.toLowerCase()) {
            case 'user':
                redirectUrl = `${this.contextPath}/profile?userId=${itemId}`;
                break;
            case 'post':
                redirectUrl = `${this.contextPath}/post?postID=${itemId}`;
                break;
            case 'group':
                redirectUrl = `${this.contextPath}/group?groupId=${itemId}`;
                break;
            default:
                console.warn('Unknown item type:', itemType);
                return;
        }

        console.log('Navigating to:', redirectUrl);
        window.location.href = redirectUrl;
    }

    performFullSearch() {
        if (this.currentKeyword && this.currentKeyword.length >= 2) {
            const fullSearchUrl = `${this.contextPath}/search?keyword=${encodeURIComponent(this.currentKeyword)}`;
            window.location.href = fullSearchUrl;
        }
    }

    navigateResults(direction) {
        const items = this.searchDropdown.querySelectorAll('.search-item');
        if (items.length === 0) return;

        let currentIndex = Array.from(items).findIndex(item => item.classList.contains('selected'));

        if (direction === 'down') {
            currentIndex = currentIndex < items.length - 1 ? currentIndex + 1 : 0;
        } else if (direction === 'up') {
            currentIndex = currentIndex > 0 ? currentIndex - 1 : items.length - 1;
        }

        // Remove previous selection
        items.forEach(item => item.classList.remove('selected'));

        // Add selection to current item
        if (items[currentIndex]) {
            items[currentIndex].classList.add('selected');
            items[currentIndex].scrollIntoView({ block: 'nearest' });
        }
    }

    showDropdown() {
        if (this.searchDropdown) {
            this.searchDropdown.classList.add('show');
        }
    }

    hideDropdown() {
        if (this.searchDropdown) {
            this.searchDropdown.classList.remove('show');
        }
    }

    showLoading() {
        if (this.searchLoading) {
            this.searchLoading.style.display = 'block';
        }
    }

    hideLoading() {
        if (this.searchLoading) {
            this.searchLoading.style.display = 'none';
        }
    }

    getContextPath() {
        const path = window.location.pathname;
        const contextPath = path.substring(0, path.indexOf("/", 2));
        return contextPath || '';
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    escapeRegex(string) {
        return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }

    // Public method để clear search
    clearSearch() {
        if (this.searchInput) {
            this.searchInput.value = '';
            this.currentKeyword = '';
            this.hideDropdown();
            this.hideLoading();
        }
    }

    // Public method để set focus vào search box
    focus() {
        if (this.searchInput) {
            this.searchInput.focus();
        }
    }
}

// Global functions for other scripts to use
window.LiveSearchAPI = {
    highlightText: function(text, searchTerm) {
        if (!searchTerm || !text) return text;
        const regex = new RegExp(`(${searchTerm.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
        return text.replace(regex, '<span class="search-highlight">$1</span>');
    },

    clearSearch: function() {
        if (window.liveSearchInstance) {
            window.liveSearchInstance.clearSearch();
        }
    },

    focusSearch: function() {
        if (window.liveSearchInstance) {
            window.liveSearchInstance.focus();
        }
    }
};

// Initialize live search when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded, initializing LiveSearch...');

    // Đợi một chút để đảm bảo tất cả elements đã load
    setTimeout(() => {
        try {
            window.liveSearchInstance = new LiveSearch();
        } catch (error) {
            console.error('Error initializing LiveSearch:', error);
        }
    }, 100);
});

// Keyboard shortcuts
document.addEventListener('keydown', function(e) {
    // Ctrl/Cmd + K để focus vào search
    if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
        e.preventDefault();
        if (window.LiveSearchAPI) {
            window.LiveSearchAPI.focusSearch();
        }
    }
});

// Export for module systems if needed
if (typeof module !== 'undefined' && module.exports) {
    module.exports = LiveSearch;
}

// Debug function
function debugLiveSearch() {
    console.log('=== Live Search Debug Info ===');
    console.log('Search Input:', document.getElementById('liveSearchInput'));
    console.log('Search Dropdown:', document.getElementById('liveSearchDropdown'));
    console.log('Search Loading:', document.getElementById('searchLoading'));
    console.log('Search Frame:', document.getElementById('liveSearchFrame'));
    console.log('Context Path:', window.location.pathname);
    console.log('LiveSearch Instance:', window.liveSearchInstance);
}

// Call debug after a short delay
setTimeout(debugLiveSearch, 2000);

