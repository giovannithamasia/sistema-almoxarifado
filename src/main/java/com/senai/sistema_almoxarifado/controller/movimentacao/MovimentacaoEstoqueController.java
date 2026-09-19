package com.senai.sistema_almoxarifado.controller.movimentacao;

import com.senai.sistema_almoxarifado.dto.MovimentacaoDto.MovimentacaoRequisicaoDto;
import com.senai.sistema_almoxarifado.entity.UsuarioEntity;
import com.senai.sistema_almoxarifado.service.MovimentacaoEstoqueService;
import com.senai.sistema_almoxarifado.sessoes.SessaoDto;
import com.senai.sistema_almoxarifado.sessoes.SessaoUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MovimentacaoEstoqueController {

    private final MovimentacaoEstoqueService service;

    @GetMapping("/movimentacoes")
    public String getMovimentacaoLista(HttpSession session, Model model){
        if (SessaoUtil.obterSessao(session) == null) {
            return "redirect:/login";
        }

        model.addAttribute("listaProdutos", service.listarProdutosCadastrados());
        model.addAttribute("listaMovimentacoes", service.listarHistoricoMovimentacoes());
        model.addAttribute("usuarioLogado", SessaoUtil.obterSessao(session));

        return "movimentacoes/listarmovimentacoes";
    }

    @PostMapping("/movimentacaocadastrar")
    public String registrarMovimentacao(
            @RequestParam(value = "produtoId", required = false) Long produtoId,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "quantidade", required = false) Integer quantidade,
            @RequestParam(value = "dataMovimentacao", required = false) String dataMovimentacaoStr,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Object usuarioNaSessao = SessaoUtil.obterSessao(session);
        if (usuarioNaSessao == null) {
            return "redirect:/login";
        }

        if (produtoId == null || tipo == null || tipo.isBlank()
                || quantidade == null || dataMovimentacaoStr == null || dataMovimentacaoStr.isBlank()) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Campos obrigatórios não preenchidos. Verifique produto, tipo, quantidade e data.");
            return "redirect:/movimentacoes";
        }

        try {
            SessaoDto usuarioSessao = (SessaoDto) usuarioNaSessao;
            UsuarioEntity usuarioLogado = new UsuarioEntity();
            usuarioLogado.setId(usuarioSessao.usuarioId());

            java.time.LocalDateTime dataMovimentacao = java.time.LocalDateTime.parse(dataMovimentacaoStr);
            MovimentacaoRequisicaoDto requisicaoDto = new MovimentacaoRequisicaoDto(produtoId, quantidade, dataMovimentacao);

            if ("ENTRADA".equalsIgnoreCase(tipo)) {
                String mensagem = service.registrarEntrada(requisicaoDto, usuarioLogado);
                redirectAttributes.addFlashAttribute("mensagemSucesso", mensagem);
            } else if ("SAIDA".equalsIgnoreCase(tipo)) {
                String mensagem = service.registrarSaida(requisicaoDto, usuarioLogado);

                if (mensagem.contains("ATENÇÃO")) {
                    redirectAttributes.addFlashAttribute("alertaMinimo", mensagem);
                } else {
                    redirectAttributes.addFlashAttribute("mensagemSucesso", mensagem);
                }
            } else {
                redirectAttributes.addFlashAttribute("mensagemErro", "Tipo de movimentação inválido.");
            }

        } catch (java.time.format.DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Data da movimentação inválida.");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Quantidade insuficiente")) {
                redirectAttributes.addFlashAttribute("erroQuantidade", e.getMessage());
            } else {
                redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro inesperado ao registrar movimentação. Verifique o console.");
        }

        return "redirect:/movimentacoes";
    }
}