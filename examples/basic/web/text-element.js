import {css, html, LitElement} from "lit";

export class TextElement extends LitElement {

    static properties = {
        count: {}
    }

    static styles = css`
        div {
            background: var(--color-primary-container);
            color: var(--color-on-primary-container)
        }
        h6 {
            font-size: 1.2rem;
            margin: 0;
        }
        h5 {
            margin: 0;
        }
    `

    constructor() {
        super();
        this.count = 0
    }

    increment() {
        this.count++
    }

    render() {
        return html`
            <div>
                <h5>Web Component</h5>
                <h6>Count: ${this.count}</h6>
                <button @click="${this.increment}">Increment</button>
            </div>
        `
    }
}

customElements.define("text-element", TextElement)
