package com.senai.sistema_almoxarifado.controller.produto;

import com.senai.sistema_almoxarifado.dto.produto.ProdutoDto;
import com.senai.sistema_almoxarifado.exceptions.ProdutoCadastradoException;
import com.senai.sistema_almoxarifado.service.ProdutoService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @PostMapping("/produtocadastrar")
    public String cadastrarProduto(@Valid @ModelAttribute("produtoDto") ProdutoDto produtoDto,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession session,
                                   Model model) {

        SessaoDto usuarioLogado = SessaoUtil.obterSessao(session);

        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "produtos/produtocadastrar";
        }

        try {
            produtoService.cadastrarProduto(produtoDto);
            redirectAttributes.addFlashAttribute("mensagemCriacao", "Produto cadastrado com sucesso!");
            return "redirect:/produtolista";
        } catch (ProdutoCadastradoException ex) {
            model.addAttribute("erroCodigoDuplicado", ex.getMessage());
            return "produtos/produtocadastrar";
        }
    }
}
