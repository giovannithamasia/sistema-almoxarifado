// Exclusão de produto (só ADMIN). Envia o token CSRF exigido pelo Spring Security.
document.addEventListener('DOMContentLoaded', function () {
  const metaToken = document.querySelector('meta[name="_csrf"]');
  const metaHeader = document.querySelector('meta[name="_csrf_header"]');
  const caixaErro = document.getElementById('mensagem-erro');

  function mostrarErro(texto) {
    if (!caixaErro) { alert(texto); return; }
    caixaErro.textContent = texto;
    caixaErro.style.display = 'block';
  }

  document.querySelectorAll('.btn-icon.excluir').forEach(function (botao) {
    botao.addEventListener('click', async function () {
      const id = botao.dataset.id;
      const nome = botao.dataset.nome;

      if (!confirm('Excluir o produto "' + nome + '"?')) return;

      const headers = {};
      if (metaToken && metaHeader) {
        headers[metaHeader.content] = metaToken.content;
      }

      try {
        const resposta = await fetch('/produtoexcluir/' + id, {
          method: 'DELETE',
          headers: headers
        });

        if (resposta.ok) {
          window.location.reload();
        } else if (resposta.status === 403) {
          mostrarErro('Você não tem permissão para excluir produtos.');
        } else {
          const texto = await resposta.text();
          mostrarErro(texto || 'Não foi possível excluir o produto.');
        }
      } catch (e) {
        mostrarErro('Erro de conexão ao excluir o produto.');
      }
    });
  });
});