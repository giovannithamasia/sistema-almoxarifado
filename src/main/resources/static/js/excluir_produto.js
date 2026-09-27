document.querySelectorAll('.excluir').forEach(function(button) {
    button.addEventListener('click', function() {
        const linha = this.closest('tr');
        const id = this.dataset.id;
        const nomeProduto = this.dataset.nome;

        if (confirm(`Tem a certeza que deseja excluir o produto ${nomeProduto}?`)) {

            const divErro = document.getElementById('mensagem-erro');
            divErro.style.display = 'none';

            fetch(`/produtoexcluir/${id}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                },
            })
            .then(async response => {
                if (response.ok) {
                    linha.remove();
                    setTimeout(() => location.reload(), 500);
                } else {
                    const mensagemErro = await response.text();

                    divErro.textContent = `Não foi possível excluir ${nomeProduto}: ${mensagemErro}`;
                    divErro.style.display = 'block';
                    window.scrollTo({ top: 0, behavior: 'smooth' });
                }
            })
            .catch(error => {
                divErro.textContent = `Erro de conexão ao tentar excluir ${nomeProduto}.`;
                divErro.style.display = 'block';
                window.scrollTo({ top: 0, behavior: 'smooth' });
            });
        }
    });
});