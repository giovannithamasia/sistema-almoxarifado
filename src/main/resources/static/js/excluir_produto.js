document.querySelectorAll('.excluir').forEach(function(button) {
    button.addEventListener('click',
        function() {
            if (confirm('Confirma a exclusão?')) {

                const linha = this.closest('tr');

                const id = this.dataset.id;

                fetch(`/produtoexcluir/${id}`, {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                })
                    .then(response => {
                        if (response.ok) {
                            console.log('Produto excluído com sucesso.');

                            linha.remove();
                        } else {
                            console.error('Erro ao excluir produto.');
                            alert('Erro ao excluir produto');
                        }
                    })
                    .catch(error => {
                        console.error('Erro de rede:', error);
                        alert('Erro de rede:' + error);
                    });
            }
        });
});