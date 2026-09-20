package com.senai.sistema_almoxarifado.controller.movimentacao;

import com.senai.sistema_almoxarifado.dto.movimentacao.MovimentacaoEstoqueDto;
import com.senai.sistema_almoxarifado.entity.UsuarioEntity;
import com.senai.sistema_almoxarifado.service.MovimentacaoEstoqueService;
import com.senai.sistema_almoxarifado.sessoes.SessaoDto;
import com.senai.sistema_almoxarifado.sessoes.SessaoUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MovimentacaoController {

    private final MovimentacaoEstoqueService service;

    @PostMapping("/movimentacaocadastrar")
    public String registrarMovimentacao(
            @Valid @ModelAttribute("movimentacaoDto") MovimentacaoEstoqueDto dto,
            BindingResult bindingResult,
            @RequestParam(value = "tipo", required = false) String tipo,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        SessaoDto usuarioSessao = SessaoUtil.obterSessao(session);
        if (usuarioSessao == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            recarregarListasNaTela(model, usuarioSessao);
            return "movimentacoes/listarmovimentacoes";
        }

        if (tipo == null || tipo.isBlank()) {
            model.addAttribute("mensagemErro", "O tipo de movimentação é obrigatório.");
            recarregarListasNaTela(model, usuarioSessao);
            return "movimentacoes/listarmovimentacoes";
        }

        try {
            UsuarioEntity usuarioLogado = new UsuarioEntity();
            usuarioLogado.setId(usuarioSessao.usuarioId());

            if ("ENTRADA".equalsIgnoreCase(tipo)) {
                service.registrarEntrada(dto, usuarioLogado);
                redirectAttributes.addFlashAttribute("mensagemSucesso", "Movimentação de entrada registrada com sucesso!");

            } else if ("SAIDA".equalsIgnoreCase(tipo)) {
                service.registrarSaida(dto, usuarioLogado);

                boolean alerta = service.verificarAlertaEstoqueMinimo(dto.produtoId());
                if (alerta) {
                    redirectAttributes.addFlashAttribute("alertaMinimo", "Movimentação registrada com sucesso! ATENÇÃO: O produto ficou abaixo do estoque mínimo.");
                } else {
                    redirectAttributes.addFlashAttribute("mensagemSucesso", "Movimentação de saída registrada com sucesso!");
                }
            } else {
                model.addAttribute("mensagemErro", "Tipo de movimentação inválido.");
                recarregarListasNaTela(model, usuarioSessao);
                return "movimentacoes/listarmovimentacoes";
            }

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Quantidade insuficiente")) {
                model.addAttribute("erroQuantidade", e.getMessage());
            } else {
                model.addAttribute("mensagemErro", e.getMessage());
            }
            recarregarListasNaTela(model, usuarioSessao);
            return "movimentacoes/listarmovimentacoes";
        }

        return "redirect:/movimentacoes";
    }

    private void recarregarListasNaTela(Model model, SessaoDto usuarioSessao) {
        model.addAttribute("listaProdutos", service.listarProdutosCadastrados());
        model.addAttribute("listaMovimentacoes", service.listarHistoricoMovimentacoes());
        model.addAttribute("usuarioLogado", usuarioSessao);
    }
}